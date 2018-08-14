package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gui.AbstractTestGuiRenderer;
import com.btxtech.shared.gui.TestGuiDisplay;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.btxtech.shared.gameengine.planet.GameTestHelper.createObstacleSlopes;

/**
 * Created by Beat
 * on 24.05.2018.
 */
public class OrcaTest {
    //    private static final DecimalPosition POINT_TRIANGLE_1 = new DecimalPosition(20, 10);
//    private static final DecimalPosition POINT_TRIANGLE_2 = new DecimalPosition(30, 20);
//    private static final DecimalPosition POINT_TRIANGLE_3 = new DecimalPosition(20, 30);
    private static final DecimalPosition POINT_TRIANGLE_1 = new DecimalPosition(10, 20);
    private static final DecimalPosition POINT_TRIANGLE_2 = new DecimalPosition(20, 10);
    private static final DecimalPosition POINT_TRIANGLE_3 = new DecimalPosition(30, 20);
    // private SyncPhysicalMovable syncPhysicalMovable1;
    // private Orca orca1;

    @Test
    public void testGui() {
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(78.25, 160.0), new DecimalPosition(-15.0, 1.83697019872103E-15));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(61.749999999999986, 158.0), new DecimalPosition(14.97953701566793, 0.7832437655251202));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(61.749999999999986, 162.0), new DecimalPosition(14.97953701566793, -0.7832437655251202));
        ///
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(27.01204246464725, 20.0), new DecimalPosition(-12.879575353527443, 1.5772930731074524E-15));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(13.24128433005918, 17.83853468818384), new DecimalPosition(15.473272819270125, 0.9098506802964705));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(13.24128433005918, 22.16146531181616), new DecimalPosition(15.473272819270127, -0.9098506802964706));
        ///
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(73.54347670132562, 160.0), new DecimalPosition(-1.0326164933719664, 1.2645904833547426E-16));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(69.67934615779934, 157.36264913700253), new DecimalPosition(16.936052765691972, 1.4731316023007792));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(69.67934615779936, 162.63735086299747), new DecimalPosition(16.936052765691972, -1.4731316023007799));
        ///
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(23.54347670132562, 20.0), new DecimalPosition(-1.0326164933719664, 1.2645904833547426E-16));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(19.67934615779934, 17.36264913700253), new DecimalPosition(16.936052765691972, 1.4731316023007792));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(19.67934615779936, 22.63735086299747), new DecimalPosition(16.936052765691972, -1.4731316023007799));
        //
        // SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(83.750, 60.000), new DecimalPosition(-12.500, 0.000), new DecimalPosition(-13.000, 0.000));


//        ObstacleSlope obstacleSlope1 = new ObstacleSlope(POINT_TRIANGLE_1, POINT_TRIANGLE_2, POINT_TRIANGLE_3, POINT_TRIANGLE_3);
//        ObstacleSlope obstacleSlope2 = new ObstacleSlope(POINT_TRIANGLE_2, POINT_TRIANGLE_3, POINT_TRIANGLE_1, POINT_TRIANGLE_1);
//        ObstacleSlope obstacleSlope3 = new ObstacleSlope(POINT_TRIANGLE_3, POINT_TRIANGLE_1, POINT_TRIANGLE_2, POINT_TRIANGLE_2);

        DebugHelperStatic.setCurrentTick(1);
        // orca1.add(syncPhysicalMovable2);
        // orca1.add(syncPhysicalMovable3);
//        System.out.println("orca1: " + orca1.getNewVelocity() + ". speed: " + orca1.getNewVelocity().magnitude() + ". angle speed: " + Math.toDegrees(orca1.getNewVelocity().angle()));
//            Orca orca2 = new Orca(syncPhysicalMovable2);
//            orca2.add(syncPhysicalMovable1);
//            orca2.add(syncPhysicalMovable3);
//            orca2.solve();
//            System.out.println("orca2: " + orca2.getNewVelocity() + ". speed: " + orca2.getNewVelocity().magnitude() + ". angle speed: " + Math.toDegrees(orca2.getNewVelocity().angle()));
//            Orca orca3 = new Orca(syncPhysicalMovable3);
//            orca3.add(syncPhysicalMovable1);
//            orca3.add(syncPhysicalMovable2);
//            orca3.solve();
//            System.out.println("orca3: " + orca3.getNewVelocity());
        DebugHelperStatic.printAfterTick(null);

        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            private DecimalPosition position = new DecimalPosition(243.07001332001306, 339.65095460095426);

            @Override
            protected void doRender() {
                List<ObstacleSlope> obstacles = new ArrayList<>();
                // obstacles.addAll(createObstacleSlopes(new DecimalPosition(20, 40), new DecimalPosition(20, 20), new DecimalPosition(40, 40)));
                // ------------- Written code -------------
                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(2909.12933213778, 1486.5008100385419), new DecimalPosition(4.119818000083237, 16.493244060711348), new DecimalPosition(4.119818000083416, 16.493244060711305), 17.0);
                obstacles.addAll(new ArrayList<>(createObstacleSlopes(
                        new DecimalPosition(2939.539954387605, 1512.2183309696397), new DecimalPosition(2938.6697117429753, 1514.8966624303646), new DecimalPosition(2937.7994690983455, 1517.5749938910892), new DecimalPosition(2936.9292264537157, 1520.253325351814), new DecimalPosition(2936.058983809086, 1522.9316568125387), new DecimalPosition(2935.1887411644566, 1525.6099882732635), new DecimalPosition(2934.318498519827, 1528.2883197339884), new DecimalPosition(2933.448255875197, 1530.966651194713), new DecimalPosition(2932.578013230567, 1533.6449826554378), new DecimalPosition(2932.387721156729, 1534.6717068079658), new DecimalPosition(2932.4696488928125, 1535.7126973962086), new DecimalPosition(2932.8182131989943, 1536.6970126318956), new DecimalPosition(2933.8161232200277, 1538.6555213227252), new DecimalPosition(2934.376877755187, 1540.3813463239812), new DecimalPosition(2934.8663125922353, 1543.4715162677307), new DecimalPosition(2935.355747429284, 1546.5616862114803), new DecimalPosition(2935.355747429284, 1548.3763260059468), new DecimalPosition(2934.8663125922353, 1551.4664959496963), new DecimalPosition(2934.376877755187, 1554.5566658934458), new DecimalPosition(2933.8161232200277, 1556.2824908947018), new DecimalPosition(2932.395728000826, 1559.070173473877), new DecimalPosition(2930.975332781624, 1561.8578560530523), new DecimalPosition(2929.9087142722133, 1563.325930485445), new DecimalPosition(2929.3815792943096, 1563.8530654633487), new DecimalPosition(2928.6841209074682, 1564.792542784851), new DecimalPosition(2927.2519632512117, 1567.4657868921495), new DecimalPosition(2926.9653209665685, 1567.944070815552), new DecimalPosition(2926.0245511343514, 1569.3552255638783), new DecimalPosition(2925.939624977095, 1569.4882263109635), new DecimalPosition(2924.5490092490986, 1571.763779320411), new DecimalPosition(2923.158393521103, 1574.039332329859), new DecimalPosition(2923.035250593081, 1574.2321834131324), new DecimalPosition(2921.650450493407, 1576.309383562644), new DecimalPosition(2921.5056791022603, 1576.5436285941173), new DecimalPosition(2919.582735959575, 1579.9093641845495), new DecimalPosition(2918.3013819476755, 1582.215801405969), new DecimalPosition(2917.332489077053, 1583.5002917360146), new DecimalPosition(2914.329552717909, 1586.503228095159), new DecimalPosition(2913.5939537459017, 1587.5156932209468), new DecimalPosition(2911.8489902973174, 1590.9403768168606), new DecimalPosition(2910.782371787907, 1592.4084512492534), new DecimalPosition(2908.5700543670823, 1594.6207686700782), new DecimalPosition(2906.3577369462573, 1596.833086090903), new DecimalPosition(2904.8896625138646, 1597.8997046003135), new DecimalPosition(2902.1019799346896, 1599.3200998195155), new DecimalPosition(2899.314297355514, 1600.7404950387174), new DecimalPosition(2897.5884723542586, 1601.3012495738767), new DecimalPosition(2894.498302410509, 1601.7906844109252), new DecimalPosition(2891.4081324667595, 1602.2801192479737), new DecimalPosition(2889.5934926722925, 1602.2801192479737), new DecimalPosition(2886.503322728543, 1601.7906844109252), new DecimalPosition(2883.4131527847935, 1601.301249573877), new DecimalPosition(2881.6873277835375, 1600.7404950387177), new DecimalPosition(2878.899645204362, 1599.3200998195157), new DecimalPosition(2876.111962625187, 1597.8997046003135), new DecimalPosition(2874.6438881927943, 1596.833086090903), new DecimalPosition(2872.4315707719693, 1594.6207686700782), new DecimalPosition(2870.2192533511447, 1592.4084512492534), new DecimalPosition(2869.152634841734, 1590.9403768168606), new DecimalPosition(2867.7322396225322, 1588.1526942376854), new DecimalPosition(2866.3118444033303, 1585.3650116585102), new DecimalPosition(2865.751089868171, 1583.6391866572542), new DecimalPosition(2865.2616550311227, 1580.5490167135047), new DecimalPosition(2864.772220194074, 1577.4588467697552), new DecimalPosition(2864.772220194074, 1575.6442069752886), new DecimalPosition(2865.2616550311227, 1572.554037031539), new DecimalPosition(2865.751089868171, 1569.4638670877896), new DecimalPosition(2865.7923143246157, 1569.2337134012214), new DecimalPosition(2866.0543190161816, 1567.9236899433922), new DecimalPosition(2866.573849094896, 1566.4280186287044), new DecimalPosition(2867.994244314098, 1563.6403360495292), new DecimalPosition(2869.4146395333, 1560.852653470354), new DecimalPosition(2870.4812580427106, 1559.3845790379612), new DecimalPosition(2872.250379856695, 1557.6154572239766), new DecimalPosition(2874.01950167068, 1555.846335409992), new DecimalPosition(2874.6403720165354, 1555.0433334157558), new DecimalPosition(2875.8572483488056, 1552.9687462701495), new DecimalPosition(2877.14480320468, 1550.6520142468717), new DecimalPosition(2879.2387060616084, 1546.9875953966607), new DecimalPosition(2880.936076774458, 1543.9845549046959), new DecimalPosition(2882.6334474873074, 1540.9815144127313), new DecimalPosition(2882.856820244725, 1540.6181853632988), new DecimalPosition(2883.3808296278567, 1539.8321712886013), new DecimalPosition(2884.7117303889886, 1537.8358201469027), new DecimalPosition(2884.895111503295, 1537.5324278847243), new DecimalPosition(2886.2589833109027, 1535.0319962374438), new DecimalPosition(2886.7107776338057, 1534.329338595391), new DecimalPosition(2887.496791708503, 1533.2813198291276), new DecimalPosition(2888.0355723776215, 1532.6601004982456), new DecimalPosition(2890.247889798446, 1530.4477830774208), new DecimalPosition(2892.460207219271, 1528.235465656596), new DecimalPosition(2892.938195274338, 1527.8076458927083), new DecimalPosition(2894.2482187321675, 1526.7596271264447), new DecimalPosition(2895.2383051094935, 1526.120828380922), new DecimalPosition(2897.196813800323, 1525.1229183598884), new DecimalPosition(2898.0573742268, 1524.5314715650445), new DecimalPosition(2898.7355340731465, 1523.7374484031952), new DecimalPosition(2899.1850778665453, 1522.7949602406347), new DecimalPosition(2900.055320511175, 1520.11662877991), new DecimalPosition(2900.925563155805, 1517.4382973191853), new DecimalPosition(2901.795805800434, 1514.7599658584604), new DecimalPosition(2902.666048445064, 1512.0816343977356), new DecimalPosition(2903.536291089694, 1509.4033029370107), new DecimalPosition(2904.406533734323, 1506.724971476286), new DecimalPosition(2905.276776378953, 1504.0466400155613), new DecimalPosition(2906.1470190235827, 1501.3683085548364), new DecimalPosition(2910.9023016050587, 1502.9133935267112), new DecimalPosition(2910.032058960429, 1505.591724987436), new DecimalPosition(2909.161816315799, 1508.2700564481606), new DecimalPosition(2908.2915736711698, 1510.9483879088855), new DecimalPosition(2907.42133102654, 1513.6267193696103), new DecimalPosition(2906.55108838191, 1516.3050508303352), new DecimalPosition(2905.680845737281, 1518.98338229106), new DecimalPosition(2904.810603092651, 1521.6617137517846), new DecimalPosition(2903.9403604480212, 1524.3400452125095), new DecimalPosition(2902.9288869128736, 1526.4606435782703), new DecimalPosition(2901.4030272585946, 1528.2471956924314), new DecimalPosition(2899.4667662990205, 1529.5779509808303), new DecimalPosition(2897.508257608191, 1530.5758610018638), new DecimalPosition(2897.3716939699393, 1530.66397117366), new DecimalPosition(2895.995741125204, 1531.7709995625287), new DecimalPosition(2893.783423704379, 1533.9833169833535), new DecimalPosition(2891.496791708503, 1536.2813198291276), new DecimalPosition(2890.6484611754745, 1537.4262568908464), new DecimalPosition(2889.284589367867, 1539.9266885381269), new DecimalPosition(2888.871981860678, 1540.609321128029), new DecimalPosition(2887.541081099546, 1542.6056722697276), new DecimalPosition(2886.9862616809082, 1543.4418006960707), new DecimalPosition(2885.2888909680587, 1546.4448411880353), new DecimalPosition(2883.570642985848, 1549.484616815201), new DecimalPosition(2881.4812876081046, 1553.1411057443984), new DecimalPosition(2880.228034729413, 1555.3969609260428), new DecimalPosition(2880.1215385531727, 1555.5833810890826), new DecimalPosition(2878.951993854787, 1557.5751148288934), new DecimalPosition(2877.5550355766127, 1559.3818693159246), new DecimalPosition(2875.785913762628, 1561.1509911299092), new DecimalPosition(2874.0167919486435, 1562.9201129438939), new DecimalPosition(2873.869672154242, 1563.1226059690516), new DecimalPosition(2872.44927693504, 1565.9102885482268), new DecimalPosition(2871.028881715838, 1568.697971127402), new DecimalPosition(2870.957222394636, 1568.9042706190833), new DecimalPosition(2870.6895315711467, 1570.2460394129907), new DecimalPosition(2870.200096734098, 1573.3362093567403), new DecimalPosition(2869.7106618970497, 1576.4263793004898), new DecimalPosition(2869.7106618970497, 1576.676674444554), new DecimalPosition(2870.200096734098, 1579.7668443883035), new DecimalPosition(2870.6895315711467, 1582.857014332053), new DecimalPosition(2870.766877024272, 1583.0950591598125), new DecimalPosition(2872.187272243474, 1585.8827417389878), new DecimalPosition(2873.607667462676, 1588.670424318163), new DecimalPosition(2873.7547872570776, 1588.8729173433208), new DecimalPosition(2875.967104677902, 1591.0852347641455), new DecimalPosition(2878.179422098727, 1593.2975521849703), new DecimalPosition(2878.3819151238845, 1593.4446719793716), new DecimalPosition(2881.1695977030595, 1594.8650671985738), new DecimalPosition(2883.957280282235, 1596.2854624177758), new DecimalPosition(2884.1953251099944, 1596.3628078709012), new DecimalPosition(2887.285495053744, 1596.8522427079495), new DecimalPosition(2890.3756649974935, 1597.341677544998), new DecimalPosition(2890.625960141558, 1597.341677544998), new DecimalPosition(2893.7161300853077, 1596.8522427079495), new DecimalPosition(2896.806300029057, 1596.362807870901), new DecimalPosition(2897.0443448568167, 1596.2854624177755), new DecimalPosition(2899.832027435992, 1594.8650671985736), new DecimalPosition(2902.619710015167, 1593.4446719793716), new DecimalPosition(2902.8222030403244, 1593.2975521849703), new DecimalPosition(2905.0345204611494, 1591.0852347641455), new DecimalPosition(2907.246837881974, 1588.8729173433208), new DecimalPosition(2907.3939576763755, 1588.670424318163), new DecimalPosition(2909.13892112496, 1585.2457407222491), new DecimalPosition(2910.7940188119755, 1582.9676941892267), new DecimalPosition(2913.79695517112, 1579.9647578300821), new DecimalPosition(2913.930595567068, 1579.7875867500757), new DecimalPosition(2915.265176784941, 1577.3866854640523), new DecimalPosition(2917.164463391638, 1574.062933902333), new DecimalPosition(2917.4901990217177, 1573.5358825815176), new DecimalPosition(2918.891984352843, 1571.4320822825891), new DecimalPosition(2920.282600080839, 1569.1565292731411), new DecimalPosition(2921.673215808835, 1566.8809762636936), new DecimalPosition(2921.8642996626622, 1566.581724582752), new DecimalPosition(2922.8446063617266, 1565.1045996380947), new DecimalPosition(2924.276764017983, 1562.4313555307963), new DecimalPosition(2925.8460453883768, 1560.317531557416), new DecimalPosition(2926.3731803662804, 1559.7903965795124), new DecimalPosition(2926.520300160682, 1559.5879035543546), new DecimalPosition(2927.940695379884, 1556.8002209751794), new DecimalPosition(2929.361090599086, 1554.0125383960042), new DecimalPosition(2929.4384360522113, 1553.7744935682447), new DecimalPosition(2929.92787088926, 1550.6843236244952), new DecimalPosition(2930.4173057263083, 1547.5941536807456), new DecimalPosition(2930.4173057263083, 1547.3438585366814), new DecimalPosition(2929.92787088926, 1544.253688592932), new DecimalPosition(2929.4384360522113, 1541.1635186491824), new DecimalPosition(2929.361090599086, 1540.9254738214229), new DecimalPosition(2928.3631805780524, 1538.9669651305933), new DecimalPosition(2927.5789108891436, 1536.7522558502974), new DecimalPosition(2927.3945734829567, 1534.410027026751), new DecimalPosition(2927.822730649091, 1532.099897683563), new DecimalPosition(2928.692973293721, 1529.4215662228382), new DecimalPosition(2929.563215938351, 1526.7432347621136), new DecimalPosition(2930.4334585829806, 1524.0649033013888), new DecimalPosition(2931.30370122761, 1521.3865718406641), new DecimalPosition(2932.1739438722398, 1518.7082403799393), new DecimalPosition(2933.0441865168696, 1516.0299089192144), new DecimalPosition(2933.9144291614994, 1513.3515774584898), new DecimalPosition(2934.784671806129, 1510.673245997765)
                )));
                // ------------- Written code ends -------------
                // ------------- Generated code -------------
                // Mouse
                // SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, position, new DecimalPosition(4.100339643125758, 16.49809730881144), new DecimalPosition(4.100339643125758, 16.49809730881144), 17.0);
                // Game
                // SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(109.180285254405, 86.6074301657302), new DecimalPosition(4.100339643125758, 16.49809730881144), new DecimalPosition(4.100339643125758, 16.49809730881144), 17.0);
                // Experiental
                // SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(104.6875, 99.25), new DecimalPosition(4.100339643125758, 16.49809730881144), new DecimalPosition(4.100339643125758, 16.49809730881144), 17.0);

//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(106.1484457143097, 101.3639176522738), new DecimalPosition(110.9037282957856, 102.9090026241486), new DecimalPosition(0.3090169943749839, -0.95105651629514179), true, new DecimalPosition(0.9510565162951563, 0.30901699437493946), true, new DecimalPosition(-0.3090169943749839, 0.9510565162951417)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(105.278024733339, 104.042797975819), new DecimalPosition(106.1484457143097, 101.3639176522738), new DecimalPosition(0.30901699437486163, -0.9510565162951816), true, new DecimalPosition(0.3090169943749839, -0.9510565162951417), true, new DecimalPosition(0.9510565162951563, 0.30901699437493946)));
                // ------------- Generated code ends -------------
                Orca orca = new Orca(syncPhysicalMovable1);
                ObstacleSlope.sort(syncPhysicalMovable1.getPosition2d(), obstacles);
                obstacles.forEach(obstacleSlope -> {
                    orca.add(obstacleSlope);
                    strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0.5, 0.1));
                });
                orca.solve();
                orca.getDebugObstacles_WRONG().forEach(obstacleSlope -> {
                    strokeObstacleSlope(obstacleSlope, 0.4, new Color(1, 1, 0, 0.8));
                });
                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.05, Color.RED);
//                strokeSyncPhysicalMovable(syncPhysicalMovable9, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable5, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable15, 0.05, Color.GREEN);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
                    // System.out.println("New Velocity: " + orca.getNewVelocity() + ". speed: " + orca.getNewVelocity().magnitude());
                }

                // System.out.println("orcaLines: " + orca.getOrcaLines().size());
                for (OrcaLine orcaLine : orca.getOrcaLines()) {
                    strokeOrcaLine(orcaLine);
                }
            }

            @Override
            protected boolean onMouseMoved(DecimalPosition position) {
//                syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, position, new DecimalPosition(12.500, 0.000), new DecimalPosition(13.000, 0.000));
//                // syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(17.5, 25.076923076923077), new DecimalPosition(12.500, 0.000), new DecimalPosition(13.000, 0.000));
//                orca1 = new Orca(syncPhysicalMovable1);
//                orca1.add(obstacleSlope);
//                orca1.solve();
                this.position = position;
                return true;
            }

            @Override
            protected void onMousePressedTerrain(DecimalPosition position) {
                System.out.println("**** " + position.getX() + ", " + position.getY());
            }

            @Override
            protected void onGenTestButtonClicked(DecimalPosition mousePosition) {
                System.out.println("Mouse position: " + mousePosition.testString());
            }
        });
    }

    @Test
    public void testSlope1() {
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 40), new DecimalPosition(10, 10), new DecimalPosition(10, 10), 17.0);
        Orca orca = new Orca(syncPhysicalMovable);
        List<ObstacleSlope> obstacles = new ArrayList<>(createObstacleSlopes(
                new DecimalPosition(20, 20),
                new DecimalPosition(60, 20),
                new DecimalPosition(60, 60),
                new DecimalPosition(100, 60),
                new DecimalPosition(100, 65),
                new DecimalPosition(55, 65),
                new DecimalPosition(55, 25),
                new DecimalPosition(20, 25)
        ));
        ObstacleSlope.sort(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        // display(orca, syncPhysicalMovable, obstacles);
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352, 11.98284), orca.getNewVelocity());
    }

    @Test
    public void testSlope2() {
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 40), new DecimalPosition(10, 10), new DecimalPosition(10, 10), 17.0);
        Orca orca = new Orca(syncPhysicalMovable);
        List<ObstacleSlope> obstacles = new ArrayList<>(createObstacleSlopes(
                new DecimalPosition(40.3, 20.0),
                new DecimalPosition(51.7, 20.2),
                new DecimalPosition(65.9, 21.2),
                new DecimalPosition(77.49999999999999, 24.0),
                new DecimalPosition(89.29999999999998, 36.2),
                new DecimalPosition(91.1, 45.2),
                new DecimalPosition(88.3, 57.80000000000001),
                new DecimalPosition(76.9, 68.6),
                new DecimalPosition(63.7, 71.4),
                new DecimalPosition(41.699999999999996, 69.2),
                new DecimalPosition(32.89999999999999, 61.39999999999999),
                new DecimalPosition(26.699999999999996, 51.2),
                new DecimalPosition(33.3, 33.2)
        ));
        ObstacleSlope.sort(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        display(orca, syncPhysicalMovable, obstacles);
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352, 11.98284), orca.getNewVelocity());
    }

    @Test
    public void testSlope3() {
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(109.327796766943, 87.280505573016), new DecimalPosition(3.847037675993762, 16.558994568496136), new DecimalPosition(3.8470376759936222, 16.558994568496168), 17.0);
        Orca orca = new Orca(syncPhysicalMovable);
        List<ObstacleSlope> obstacles = new ArrayList<>(createObstacleSlopes(
                new DecimalPosition(105.278024733339, 104.042797975819),
                new DecimalPosition(106.1484457143097, 101.3639176522738),
                new DecimalPosition(110.9037282957856, 102.9090026241486),
                new DecimalPosition(110.033307314815, 105.5878829476937),
                new DecimalPosition(104.4076037523687, 106.721678299364)
        ));

        ObstacleSlope.sort(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        display(orca, syncPhysicalMovable, obstacles);
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352, 11.98284), orca.getNewVelocity());
    }

    private void display(Orca orca, SyncPhysicalMovable syncPhysicalMovable, List<ObstacleSlope> obstacles) {
        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            @Override
            protected void doRender() {
                obstacles.forEach(obstacleSlope -> {
                    orca.add(obstacleSlope);
                    strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0.5, 0.1));
                });
                orca.solve();
                orca.getDebugObstacles_WRONG().forEach(obstacleSlope -> {
                    strokeObstacleSlope(obstacleSlope, 0.4, new Color(1, 1, 0, 0.8));
                });
                strokeSyncPhysicalMovable(syncPhysicalMovable, 0.05, Color.RED);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
                }

                // System.out.println("orcaLines: " + orca.getOrcaLines().size());
                for (OrcaLine orcaLine : orca.getOrcaLines()) {
                    strokeOrcaLine(orcaLine);
                }
            }

            @Override
            protected void onMousePressedTerrain(DecimalPosition position) {
                System.out.println("new DecimalPosition(" + position.getX() + ", " + position.getY() + "),");
            }
        });
    }
}