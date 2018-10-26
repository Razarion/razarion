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
//                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, position, new DecimalPosition(10, 10), new DecimalPosition(10, 10), 17.0);
//                obstacles.addAll(createObstacleSlopes(
//                        new DecimalPosition(20, 20),
//                        new DecimalPosition(60, 20),
//                        new DecimalPosition(60, 60),
//                        new DecimalPosition(100, 60),
//                        new DecimalPosition(100, 65),
//                        new DecimalPosition(55, 65),
//                        new DecimalPosition(55, 25),
//                        new DecimalPosition(20, 25)
//                ));
                // DecimalPosition vAndPreferred = new DecimalPosition(4.119818000083237, 16.493244060711348);
                DecimalPosition vAndPreferred = new DecimalPosition(1, 1).normalize(17);
                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, position, vAndPreferred, vAndPreferred, 17.0);
//                obstacles.addAll(createObstacleSlopes(
//                        new DecimalPosition(20, 20),
//                        new DecimalPosition(40, 20),
//                        new DecimalPosition(40, 40),
//                        new DecimalPosition(20, 40),
//                        new DecimalPosition(20, 35),
//                        new DecimalPosition(20, 30),
//                        new DecimalPosition(20, 25)
//                ));


                 obstacles.addAll(createObstacleSlopes(new DecimalPosition(99.53995438760512, 32.21833096963974), new DecimalPosition(98.66971174297532, 34.896662430364586), new DecimalPosition(97.79946909834553, 37.57499389108921), new DecimalPosition(96.92922645371573, 40.253325351814055), new DecimalPosition(96.05898380908593, 42.93165681253868), new DecimalPosition(95.18874116445659, 45.609988273263525), new DecimalPosition(94.31849851982679, 48.288319733988374), new DecimalPosition(93.44825587519699, 50.966651194712995), new DecimalPosition(92.5780132305672, 53.64498265543784), new DecimalPosition(92.38772115672919, 54.67170680796585), new DecimalPosition(92.46964889281253, 55.71269739620857), new DecimalPosition(92.81821319899427, 56.69701263189563), new DecimalPosition(93.81612322002775, 58.655521322725235), new DecimalPosition(94.3768777551868, 60.381346323981234), new DecimalPosition(94.86631259223532, 63.47151626773075), new DecimalPosition(95.35574742928384, 66.56168621148026), new DecimalPosition(95.35574742928384, 68.3763260059468), new DecimalPosition(94.86631259223532, 71.46649594969631), new DecimalPosition(94.3768777551868, 74.55666589344582), new DecimalPosition(93.81612322002775, 76.28249089470182), new DecimalPosition(92.39572800082578, 79.07017347387705), new DecimalPosition(90.97533278162382, 81.85785605305227), new DecimalPosition(89.90871427221327, 83.32593048544504), new DecimalPosition(89.38157929430963, 83.85306546334868), new DecimalPosition(88.68412090746824, 84.7925427848511), new DecimalPosition(87.25196325121169, 87.46578689214948), new DecimalPosition(86.96532096656847, 87.9440708155521), new DecimalPosition(86.02455113435144, 89.35522556387832), new DecimalPosition(85.93962497709481, 89.48822631096346), new DecimalPosition(84.54900924909862, 91.763779320411), new DecimalPosition(83.15839352110288, 94.03933232985901), new DecimalPosition(83.03525059308095, 94.23218341313236), new DecimalPosition(81.65045049340688, 96.30938356264392), new DecimalPosition(81.50567910226027, 96.54362859411731), new DecimalPosition(79.58273595957507, 99.90936418454953), new DecimalPosition(78.30138194767551, 102.21580140596893), new DecimalPosition(77.3324890770532, 103.50029173601456), new DecimalPosition(74.32955271790888, 106.5032280951591), new DecimalPosition(73.59395374590167, 107.51569322094679), new DecimalPosition(71.84899029731741, 110.94037681686063), new DecimalPosition(70.78237178790687, 112.4084512492534), new DecimalPosition(68.57005436708232, 114.62076867007818), new DecimalPosition(66.35773694625732, 116.83308609090295), new DecimalPosition(64.88966251386455, 117.8997046003135), new DecimalPosition(62.10197993468955, 119.32009981951546), new DecimalPosition(59.3142973555141, 120.74049503871743), new DecimalPosition(57.588472354258556, 121.3012495738767), new DecimalPosition(54.49830241050904, 121.79068441092522), new DecimalPosition(51.408132466759525, 122.28011924797374), new DecimalPosition(49.59349267229254, 122.28011924797374), new DecimalPosition(46.503322728543026, 121.79068441092522), new DecimalPosition(43.41315278479351, 121.30124957387693), new DecimalPosition(41.68732778353751, 120.74049503871765), new DecimalPosition(38.89964520436206, 119.32009981951569), new DecimalPosition(36.11196262518706, 117.8997046003135), new DecimalPosition(34.64388819279429, 116.83308609090295), new DecimalPosition(32.43157077196929, 114.62076867007818), new DecimalPosition(30.219253351144744, 112.4084512492534), new DecimalPosition(29.152634841734198, 110.94037681686063), new DecimalPosition(27.732239622532234, 108.15269423768541), new DecimalPosition(26.31184440333027, 105.36501165851018), new DecimalPosition(25.75108986817122, 103.63918665725419), new DecimalPosition(25.2616550311227, 100.54901671350467), new DecimalPosition(24.77222019407418, 97.45884676975516), new DecimalPosition(24.77222019407418, 95.64420697528863), new DecimalPosition(25.2616550311227, 92.55403703153911), new DecimalPosition(25.75108986817122, 89.4638670877896), new DecimalPosition(25.792314324615745, 89.2337134012214), new DecimalPosition(26.05431901618158, 87.92368994339222), new DecimalPosition(26.573849094896104, 86.42801862870442), new DecimalPosition(27.99424431409807, 83.6403360495292), new DecimalPosition(29.414639533300033, 80.85265347035397), new DecimalPosition(30.48125804271058, 79.3845790379612), new DecimalPosition(32.25037985669496, 77.61545722397659), new DecimalPosition(34.0195016706798, 75.84633540999198), new DecimalPosition(34.640372016535366, 75.0433334157558), new DecimalPosition(35.85724834880557, 72.9687462701495), new DecimalPosition(37.1448032046801, 70.65201424687166), new DecimalPosition(39.238706061608355, 66.98759539666071), new DecimalPosition(40.93607677445789, 63.98455490469587), new DecimalPosition(42.63344748730742, 60.98151441273126), new DecimalPosition(42.856820244724986, 60.618185363298835), new DecimalPosition(43.380829627856656, 59.83217128860133), new DecimalPosition(44.7117303889886, 57.83582014690273), new DecimalPosition(44.895111503295084, 57.53242788472426), new DecimalPosition(46.25898331090275, 55.031996237443764), new DecimalPosition(46.71077763380572, 54.32933859539094), new DecimalPosition(47.496791708503224, 53.2813198291276), new DecimalPosition(48.03557237762152, 52.66010049824558), new DecimalPosition(50.24788979844607, 50.44778307742081), new DecimalPosition(52.46020721927107, 48.235465656596034), new DecimalPosition(52.938195274337886, 47.80764589270825), new DecimalPosition(54.248218732167516, 46.759627126444684), new DecimalPosition(55.23830510949347, 46.12082838092192), new DecimalPosition(57.19681380032307, 45.122918359888445), new DecimalPosition(58.05737422680022, 44.53147156504451), new DecimalPosition(58.73553407314648, 43.73744840319523), new DecimalPosition(59.18507786654527, 42.79496024063474), new DecimalPosition(60.05532051117507, 40.11662877990989), new DecimalPosition(60.925563155804866, 37.43829731918527), new DecimalPosition(61.79580580043421, 34.75996585846042), new DecimalPosition(62.66604844506401, 32.08163439773557), new DecimalPosition(63.536291089693805, 29.40330293701072), new DecimalPosition(64.40653373432315, 26.7249714762861), new DecimalPosition(65.27677637895295, 24.046640015561252), new DecimalPosition(66.14701902358274, 21.368308554836403), new DecimalPosition(70.90230160505871, 22.91339352671116), new DecimalPosition(70.03205896042891, 25.59172498743601), new DecimalPosition(69.16181631579911, 28.27005644816063), new DecimalPosition(68.29157367116977, 30.94838790888548), new DecimalPosition(67.42133102653997, 33.62671936961033), new DecimalPosition(66.55108838191018, 36.305050830335176), new DecimalPosition(65.68084573728083, 38.983382291060025), new DecimalPosition(64.81060309265104, 41.661713751784646), new DecimalPosition(63.94036044802124, 44.340045212509494), new DecimalPosition(62.92888691287362, 46.460643578270265), new DecimalPosition(61.40302725859465, 48.24719569243143), new DecimalPosition(59.46676629902049, 49.57795098083034), new DecimalPosition(57.50825760819089, 50.575861001863814), new DecimalPosition(57.371693969939315, 50.66397117366), new DecimalPosition(55.99574112520395, 51.77099956252869), new DecimalPosition(53.78342370437895, 53.98331698335346), new DecimalPosition(51.496791708503224, 56.2813198291276), new DecimalPosition(50.648461175474495, 57.42625689084639), new DecimalPosition(49.28458936786683, 59.92668853812688), new DecimalPosition(48.871981860677806, 60.60932112802902), new DecimalPosition(47.54108109954586, 62.60567226972762), new DecimalPosition(46.98626168090823, 63.441800696070686), new DecimalPosition(45.288890968058695, 66.4448411880353), new DecimalPosition(43.570642985847826, 69.48461681520098), new DecimalPosition(41.481287608104594, 73.14110574439837), new DecimalPosition(40.22803472941314, 75.39696092604277), new DecimalPosition(40.12153855317274, 75.58338108908265), new DecimalPosition(38.951993854786906, 77.57511482889345), new DecimalPosition(37.55503557661268, 79.38186931592463), new DecimalPosition(35.78591376262784, 81.15099112990924), new DecimalPosition(34.01679194864346, 82.92011294389386), new DecimalPosition(33.869672154241925, 83.12260596905162), new DecimalPosition(32.44927693503996, 85.91028854822684), new DecimalPosition(31.028881715837997, 88.69797112740207), new DecimalPosition(30.95722239463612, 88.90427061908326), new DecimalPosition(30.689531571146745, 90.24603941299074), new DecimalPosition(30.200096734098224, 93.33620935674026), new DecimalPosition(29.710661897049704, 96.42637930048977), new DecimalPosition(29.710661897049704, 96.67667444455401), new DecimalPosition(30.200096734098224, 99.76684438830353), new DecimalPosition(30.689531571146745, 102.85701433205304), new DecimalPosition(30.766877024272162, 103.09505915981254), new DecimalPosition(32.187272243474126, 105.88274173898776), new DecimalPosition(33.60766746267609, 108.67042431816299), new DecimalPosition(33.75478725707762, 108.87291734332075), new DecimalPosition(35.96710467790217, 111.08523476414553), new DecimalPosition(38.17942209872717, 113.2975521849703), new DecimalPosition(38.38191512388448, 113.4446719793716), new DecimalPosition(41.16959770305948, 114.8650671985738), new DecimalPosition(43.95728028223493, 116.28546241777576), new DecimalPosition(44.19532510999443, 116.36280787090118), new DecimalPosition(47.285495053743944, 116.85224270794947), new DecimalPosition(50.37566499749346, 117.34167754499799), new DecimalPosition(50.62596014155815, 117.34167754499799), new DecimalPosition(53.71613008530767, 116.85224270794947), new DecimalPosition(56.806300029057184, 116.36280787090095), new DecimalPosition(57.04434485681668, 116.28546241777553), new DecimalPosition(59.83202743599213, 114.86506719857357), new DecimalPosition(62.61971001516713, 113.4446719793716), new DecimalPosition(62.82220304032444, 113.2975521849703), new DecimalPosition(65.03452046114944, 111.08523476414553), new DecimalPosition(67.24683788197399, 108.87291734332075), new DecimalPosition(67.39395767637552, 108.67042431816299), new DecimalPosition(69.13892112495978, 105.24574072224914), new DecimalPosition(70.79401881197555, 102.96769418922668), new DecimalPosition(73.79695517111986, 99.96475783008214), new DecimalPosition(73.93059556706794, 99.78758675007566), new DecimalPosition(75.2651767849411, 97.38668546405233), new DecimalPosition(77.16446339163804, 94.06293390233304), new DecimalPosition(77.49019902171767, 93.53588258151763), new DecimalPosition(78.89198435284288, 91.43208228258914), new DecimalPosition(80.28260008083907, 89.15652927314113), new DecimalPosition(81.67321580883481, 86.88097626369358), new DecimalPosition(81.86429966266223, 86.58172458275203), new DecimalPosition(82.84460636172662, 85.10459963809467), new DecimalPosition(84.27676401798317, 82.43135553079628), new DecimalPosition(85.84604538837675, 80.31753155741603), new DecimalPosition(86.3731803662804, 79.79039657951239), new DecimalPosition(86.52030016068193, 79.58790355435463), new DecimalPosition(87.94069537988389, 76.8002209751794), new DecimalPosition(89.36109059908586, 74.01253839600417), new DecimalPosition(89.43843605221127, 73.77449356824468), new DecimalPosition(89.9278708892598, 70.68432362449516), new DecimalPosition(90.41730572630831, 67.59415368074565), new DecimalPosition(90.41730572630831, 67.34385853668141), new DecimalPosition(89.9278708892598, 64.2536885929319), new DecimalPosition(89.43843605221127, 61.16351864918238), new DecimalPosition(89.36109059908586, 60.925473821422884), new DecimalPosition(88.36318057805238, 58.96696513059328), new DecimalPosition(87.57891088914357, 56.75225585029739), new DecimalPosition(87.39457348295673, 54.410027026751095), new DecimalPosition(87.82273064909123, 52.099897683563086), new DecimalPosition(88.69297329372102, 49.42156622283824), new DecimalPosition(89.56321593835082, 46.743234762113616), new DecimalPosition(90.43345858298062, 44.06490330138877), new DecimalPosition(91.30370122760996, 41.386571840664146), new DecimalPosition(92.17394387223976, 38.7082403799393), new DecimalPosition(93.04418651686956, 36.02990891921445), new DecimalPosition(93.91442916149936, 33.35157745848983), new DecimalPosition(94.78467180612915, 30.67324599776498)));
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
                ObstacleSlope.sortObstacleSlope(syncPhysicalMovable1.getPosition2d(), obstacles);
                int size = obstacles.size() > 10 ? 10 : obstacles.size();
                double factor = 1.0 / (double)size;
                for (int i = 0; i < size; i++) {
                    strokeObstacleSlope(obstacles.get(i), 0.5, new Color(0.0, 1.0 - factor * (double)i, 0.0, 1.0));
                }


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
        ObstacleSlope.sortObstacleSlope(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        display(orca, syncPhysicalMovable, obstacles);
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
        ObstacleSlope.sortObstacleSlope(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        display(orca, syncPhysicalMovable, obstacles);
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352, 11.98284), orca.getNewVelocity());
    }

    @Test
    public void testSlope3() {
        DecimalPosition vAndPreferred = new DecimalPosition(1, 1).normalize(17);
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(17, 20), vAndPreferred, vAndPreferred, 17.0);
        Orca orca = new Orca(syncPhysicalMovable);
        List<ObstacleSlope> obstacles = createObstacleSlopes(
                new DecimalPosition(20, 20),
                new DecimalPosition(40, 20),
                new DecimalPosition(40, 40),
                new DecimalPosition(20, 40),
                new DecimalPosition(20, 35),
                new DecimalPosition(20, 30),
                new DecimalPosition(20, 25)
        );

        ObstacleSlope.sortObstacleSlope(syncPhysicalMovable.getPosition2d(), obstacles);
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