package com.btxtech.server.user;

import com.btxtech.server.frontend.LoginResult;
import com.btxtech.server.persistence.history.ForgotPasswordHistoryEntity;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.system.FilePropertiesService;
import com.btxtech.server.system.ServerI18nHelper;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.system.ExceptionHandler;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: beat
 * Date: 26.12.12
 * Time: 10:17
 */
@Singleton
public class RegisterService {
    public static final String NO_REPLY_EMAIL = "no-reply@razarion.com";
    public static final String PERSONAL_NAME = "Razarion";
    private static final String COOKIE_DELIMITER = ";";
    private static long CLEANUP_DELAY = 24 * 60 * 60 * 1000; // Is used in test cases
    @Resource(name = "DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService scheduleExecutor;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private FilePropertiesService filePropertiesService;
    @Inject
    private ServerI18nHelper serverI18nHelper;
    @Resource(mappedName = "java:/RazarionMail")
    private Session mailSession;
    @Inject
    private Logger logger;
    @Inject
    private Instance<HistoryPersistence> historyPersistence;
    @Inject
    private Instance<UserService> userService;
    @Inject
    private SessionHolder sessionHolder;
    private ScheduledFuture cleanupFuture;

    @PostConstruct
    public void init() {
        cleanupFuture = scheduleExecutor.scheduleAtFixedRate(() -> {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.add(GregorianCalendar.DAY_OF_YEAR, -1);
            Date removeIfSmaller = Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant());

            // TODO HibernateUtil.openSession4InternalCall(sessionFactory);
            try {
                removeUnverifiedUsers(removeIfSmaller);
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            } finally {
                // TODO HibernateUtil.closeSession4InternalCall(sessionFactory);
            }
            // TODO HibernateUtil.openSession4InternalCall(sessionFactory);
            try {
                removeOldPasswordForgetEntries(removeIfSmaller);
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            } finally {
                // TODO HibernateUtil.closeSession4InternalCall(sessionFactory);
            }
        }, CLEANUP_DELAY, CLEANUP_DELAY, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void cleanup() {
        if (cleanupFuture != null) {
            cleanupFuture.cancel(false);
            cleanupFuture = null;
        }
    }

    public String generateSHA512SecurePassword(String passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(filePropertiesService.getPasswordHashSalt().getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifySHA512SecurePassword(String expectedPasswordHash, String actualPassword) {
        String actualPasswordHash = generateSHA512SecurePassword(actualPassword);
        return expectedPasswordHash.equals(actualPasswordHash);
    }

    @Transactional
    public void startEmailVerifyingProcess(UserEntity userEntity) {
        userEntity.startVerification();
        sendEmailVerifyEmail(userEntity);
        entityManager.persist(userEntity);
    }

    public void sendEmailVerifyEmail(UserEntity userEntity) {
        try {
            MimeMessage msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(NO_REPLY_EMAIL, PERSONAL_NAME));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(userEntity.getEmail()));
            msg.setSubject(serverI18nHelper.getString("emailSubject", userEntity.getLocale()));
            // Setup template
            Template template = createTemplate("registration-confirmation.ftl");
            Map<String, String> paramMap = new HashMap<>();
            // paramMap.put("greeting", serverI18nHelper.getString("emailVeriGreeting", new Object[]{user.getUsername()}));
            paramMap.put("main", serverI18nHelper.getString("emailVeriMain", userEntity.getLocale()));
            paramMap.put("link", CommonUrl.generateVerificationLink(userEntity.getVerificationId()));
            // paramMap.put("user", serverI18nHelper.getString("emailVeriUser", new Object[]{user.getUsername()}));
            paramMap.put("closing", serverI18nHelper.getString("emailVeriClosing", userEntity.getLocale()));
            paramMap.put("razarionTeam", serverI18nHelper.getString("emailVeriRazarionTeam", userEntity.getLocale()));
            Writer stringWriter = new StringWriter();
            template.process(paramMap, stringWriter);

            msg.setText(stringWriter.toString(), "UTF-8", "html");
            msg.saveChanges();
            Transport.send(msg);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Transactional
    public void onEmailVerificationPageCalled(String verificationId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> criteriaQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.isNull(from.get(UserEntity_.facebookUserId)),
                criteriaBuilder.isNull(from.get(UserEntity_.verificationDoneDate)),
                criteriaBuilder.isNull(from.get(UserEntity_.verificationTimedOutDate)),
                criteriaBuilder.equal(from.get(UserEntity_.verificationId), verificationId)));
        List<UserEntity> users = entityManager.createQuery(criteriaQuery).getResultList();
        if (users == null) {
            throw new IllegalArgumentException("No unvalidated user found for verificationId: " + verificationId);
        }
        if (users.size() != 1) {
            logger.warning("More than one unvalidated user found verificationId: " + verificationId);
        }
        UserEntity userEntity = users.get(0);
        userEntity.setVerifiedDone();
        entityManager.merge(userEntity);
        // TODO update session
        // TODO send to client
    }

    @Transactional
    public void onForgotPassword(String email) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> criteriaQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.equal(from.get(UserEntity_.email), email));
        List<UserEntity> users = entityManager.createQuery(criteriaQuery).getResultList();
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("No user found for email: " + email);
        }
        if (users.size() != 1) {
            logger.warning("onForgotPassword: wrong users count (" + users.size() + ") found for email: " + email);
        }
        UserEntity userEntity = users.get(0);
        String uuid = UUID.randomUUID().toString().toUpperCase();
        saveForgotPassword(userEntity, uuid);
        sendEmailForgotPassword(userEntity, CommonUrl.generateForgotPasswordLink(uuid));
    }

    private void saveForgotPassword(UserEntity userEntity, String uuid) {
        // Remove old
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ForgotPasswordEntity> criteriaQuery = criteriaBuilder.createQuery(ForgotPasswordEntity.class);
        Root<ForgotPasswordEntity> from = criteriaQuery.from(ForgotPasswordEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.equal(from.get(ForgotPasswordEntity_.user), userEntity));
        List<ForgotPasswordEntity> forgots = entityManager.createQuery(criteriaQuery).getResultList();
        if (forgots != null) {
            forgots.forEach(forgotPasswordEntity -> {
                historyPersistence.get().onForgotPassword(userEntity, forgotPasswordEntity, ForgotPasswordHistoryEntity.Type.OVERRIDDEN);
                entityManager.remove(forgotPasswordEntity);
            });
        }
        // make new
        ForgotPasswordEntity forgot = new ForgotPasswordEntity();
        forgot.setDate(new Date());
        forgot.setUser(userEntity);
        forgot.setUuid(uuid);
        entityManager.persist(forgot);
        historyPersistence.get().onForgotPassword(userEntity, forgot, ForgotPasswordHistoryEntity.Type.INITIATED);
    }

    @Transactional
    public void onPasswordReset(String uuid, String password) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ForgotPasswordEntity> criteriaQuery = criteriaBuilder.createQuery(ForgotPasswordEntity.class);
        Root<ForgotPasswordEntity> from = criteriaQuery.from(ForgotPasswordEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.equal(from.get(ForgotPasswordEntity_.uuid), uuid));
        List<ForgotPasswordEntity> forgots = entityManager.createQuery(criteriaQuery).getResultList();
        SingleHolder<Integer> foundCount = new SingleHolder<>(0);
        SingleHolder<String> email = new SingleHolder<>();
        if (forgots == null || forgots.isEmpty()) {
            throw new IllegalArgumentException("No ForgotPasswordEntity found for uuid: " + uuid);
        }
        forgots.forEach(forgotPasswordEntity -> {
            foundCount.setO(foundCount.getO() + 1);
            email.setO(forgotPasswordEntity.getUser().getEmail());
            forgotPasswordEntity.getUser().setPasswordHash(generateSHA512SecurePassword(password));
            historyPersistence.get().onForgotPassword(forgotPasswordEntity.getUser(), forgotPasswordEntity, ForgotPasswordHistoryEntity.Type.CHANGED);
            entityManager.remove(forgotPasswordEntity);
        });
        if (foundCount.getO() != 1) {
            logger.warning("Unexpected ForgotPasswordEntity count '" + foundCount.getO() + "' for uuid: " + uuid);
        }
        if (!sessionHolder.isLoggedIn()) {
            LoginResult loginResult = userService.get().loginUser(email.getO(), password);
            if (loginResult != LoginResult.OK) {
                throw new IllegalArgumentException("Can not login user with email '" + email.getO() + "'.  LoginResult: " + loginResult);
            }
        }
    }

    private void sendEmailForgotPassword(UserEntity userEntity, String link) {
        try {
            MimeMessage msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(NO_REPLY_EMAIL, PERSONAL_NAME));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(userEntity.getEmail()));
            msg.setSubject(serverI18nHelper.getString("emailForgotPasswordSubject", userEntity.getLocale()));
            // Setup template
            Template template = createTemplate("forgot-password.ftl");
            Map<String, String> paramMap = new HashMap<>();
            // falls vorhanden context.put("greeting", serverI18nHelper.getString("emailVeriGreeting", new Object[]{user.getUsername()}));
            paramMap.put("main1", serverI18nHelper.getString("emailForgotPasswordSubjectMain1", userEntity.getLocale()));
            paramMap.put("main2", serverI18nHelper.getString("emailForgotPasswordSubjectMain2", userEntity.getLocale()));
            paramMap.put("link", link);
            paramMap.put("razarionTeam", serverI18nHelper.getString("emailVeriRazarionTeam", userEntity.getLocale()));
            Writer stringWriter = new StringWriter();
            template.process(paramMap, stringWriter);

            msg.setText(stringWriter.toString(), "UTF-8", "html");
            msg.saveChanges();
            Transport.send(msg);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void removeUnverifiedUsers(Date removeIfSmaller) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> criteriaQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.isNull(from.get(UserEntity_.facebookUserId)),
                criteriaBuilder.isNull(from.get(UserEntity_.verificationDoneDate)),
                criteriaBuilder.isNull(from.get(UserEntity_.verificationTimedOutDate)),
                criteriaBuilder.lessThanOrEqualTo(from.get(UserEntity_.verificationStartedDate), removeIfSmaller)));

        List<UserEntity> users = entityManager.createQuery(criteriaQuery).getResultList();
        if (users == null) {
            return;
        }
        users.forEach(userEntity -> {
            userEntity.setVerifiedTimedOut();
            entityManager.merge(userEntity);
        });
    }

    private void removeOldPasswordForgetEntries(Date removeIfSmaller) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ForgotPasswordEntity> criteriaQuery = criteriaBuilder.createQuery(ForgotPasswordEntity.class);
        Root<ForgotPasswordEntity> from = criteriaQuery.from(ForgotPasswordEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.lessThanOrEqualTo(from.get(ForgotPasswordEntity_.date), removeIfSmaller));
        List<ForgotPasswordEntity> forgots = entityManager.createQuery(criteriaQuery).getResultList();
        if (forgots != null) {
            forgots.forEach(forgotPasswordEntity -> {
                historyPersistence.get().onForgotPassword(forgotPasswordEntity.getUser(), forgotPasswordEntity, ForgotPasswordHistoryEntity.Type.TIMED_OUT);
                entityManager.remove(forgotPasswordEntity);
            });
        }
    }

    private Template createTemplate(String name) throws IOException {
        Configuration cfg = new Configuration(Configuration.getVersion());
        //Assume that the template is available under /src/main/resources/templates
        cfg.setClassForTemplateLoading(getClass(), "/templates/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return cfg.getTemplate(name);
    }

    /**
     * Inspired by: http://jaspan.com/improved_persistent_login_cookie_best_practice
     * Cookie stealing protection not implemented
     *
     * @param cookieValue cookei value
     */
    @Transactional
    public String cookieLogin(String cookieValue) {
        String composite = new String(Base64.getDecoder().decode(cookieValue));
        String[] parts = composite.split(COOKIE_DELIMITER);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Unable to split decoded cookieValue: " + composite);
        }
        String email = new String(Base64.getDecoder().decode(parts[0]));
        String token = new String(Base64.getDecoder().decode(parts[1]));
        if (!removeLoginCookieEntry(email, token)) {
            return null;
        }
        userService.get().autoLoginUser(email);
        return setupLoginCookieEntry(email);
    }

    private boolean removeLoginCookieEntry(String email, String token) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoginCookieEntity> criteriaQuery = criteriaBuilder.createQuery(LoginCookieEntity.class);
        Root<LoginCookieEntity> from = criteriaQuery.from(LoginCookieEntity.class);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(from.get(LoginCookieEntity_.user).get(UserEntity_.email), email),
                criteriaBuilder.equal(from.get(LoginCookieEntity_.token), token)));
        List<LoginCookieEntity> loginCookieEntities = entityManager.createQuery(criteriaQuery).getResultList();
        if (loginCookieEntities == null || loginCookieEntities.isEmpty()) {
            return false;
        }
        if (loginCookieEntities.size() != 1) {
            logger.warning("More the one LoginCookieEntity for email: " + email + " token: " + token);
        }
        loginCookieEntities.forEach(loginCookieEntity -> entityManager.remove(loginCookieEntity));
        return true;
    }

    @Transactional
    public String setupLoginCookieEntry(String email) {
        String token = UUID.randomUUID().toString();
        // Save in DB
        LoginCookieEntity loginCookieEntity = new LoginCookieEntity();
        loginCookieEntity.setTimeStamp(new Date());
        loginCookieEntity.setUser(userService.get().getUserEntity4Email(email));
        loginCookieEntity.setToken(token);
        entityManager.persist(loginCookieEntity);
        //  Encode
        String part0 = new String(Base64.getEncoder().encode(email.getBytes()));
        String part1 = new String(Base64.getEncoder().encode(token.getBytes()));
        return new String(Base64.getEncoder().encode((part0 + COOKIE_DELIMITER + part1).getBytes()));
    }
}