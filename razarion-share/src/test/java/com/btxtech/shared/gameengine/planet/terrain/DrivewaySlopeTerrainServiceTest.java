package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class DrivewaySlopeTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testEdge() {
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(103, 40, null),
                GameTestHelper.createTerrainSlopeCorner(103, 60, 1), GameTestHelper.createTerrainSlopeCorner(103, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(103, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testDrivewayEdge1.json");
        showDisplay();
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayEdge1.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    @Test
    public void testCorner1() {
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(50, 150, null),
                GameTestHelper.createTerrainSlopeCorner(70, 150, 1), GameTestHelper.createTerrainSlopeCorner(90, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 160, 1), GameTestHelper.createTerrainSlopeCorner(100, 180, 1),// driveway
                GameTestHelper.createTerrainSlopeCorner(100, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testDrivewayCorner1.json");
        showDisplay();
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayCorner1.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    @Test
    public void testCorner2() {
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(50, 150, null),
                GameTestHelper.createTerrainSlopeCorner(70, 150, 1), GameTestHelper.createTerrainSlopeCorner(90, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 160, 1), GameTestHelper.createTerrainSlopeCorner(150, 180, 1),// driveway
                GameTestHelper.createTerrainSlopeCorner(100, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testDrivewayCorner1.json");
        //----------------------------------------------------------------------------
        Polygon2D flatPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(81.09457190436244, 109.0224699279434), new DecimalPosition(81.97861303614292, 107.6317710240318), new DecimalPosition(83.34827918758309, 106.71548133314812), new DecimalPosition(84.97110615400469, 106.42911652896377), new DecimalPosition(86.57166245184291, 106.82127937966615), new DecimalPosition(91.12009681634598, 109.00584656452072), new DecimalPosition(95.66853118084904, 111.1904137493753), new DecimalPosition(100.21696554535211, 113.37498093422987), new DecimalPosition(104.76539990985518, 115.55954811908444), new DecimalPosition(109.31383427435824, 117.74411530393901), new DecimalPosition(113.86226863886131, 119.92868248879358), new DecimalPosition(118.41070300336438, 122.11324967364816), new DecimalPosition(122.95913736786744, 124.29781685850273), new DecimalPosition(127.50757173237051, 126.48238404335729), new DecimalPosition(132.05600609687357, 128.66695122821187), new DecimalPosition(136.60444046137664, 130.85151841306643), new DecimalPosition(141.1528748258797, 133.03608559792102), new DecimalPosition(145.70130919038277, 135.22065278277557), new DecimalPosition(150.24974355488584, 137.40521996763016), new DecimalPosition(154.79817791938893, 139.58978715248472), new DecimalPosition(159.34661228389197, 141.7743543373393), new DecimalPosition(163.89504664839504, 143.95892152219386), new DecimalPosition(165.39749396567507, 145.2109249910246), new DecimalPosition(166.12678846087164, 147.0255847846063), new DecimalPosition(165.90858952146692, 148.96909960317825), new DecimalPosition(169.6539062317431, 150.37359336953182), new DecimalPosition(170.09030411055255, 146.48656373238794), new DecimalPosition(168.63171512015936, 142.85724414522448), new DecimalPosition(165.62682048559932, 140.35323720756304), new DecimalPosition(161.07838612109626, 138.16867002270848), new DecimalPosition(156.5299517565932, 135.9841028378539), new DecimalPosition(151.98151739209013, 133.79953565299934), new DecimalPosition(147.43308302758706, 131.61496846814475), new DecimalPosition(142.884648663084, 129.4304012832902), new DecimalPosition(138.33621429858093, 127.24583409843561), new DecimalPosition(133.78777993407786, 125.06126691358105), new DecimalPosition(129.2393455695748, 122.87669972872646), new DecimalPosition(124.69091120507173, 120.6921325438719), new DecimalPosition(120.14247684056868, 118.50756535901733), new DecimalPosition(115.59404247606561, 116.32299817416276), new DecimalPosition(111.04560811156253, 114.13843098930819), new DecimalPosition(106.49717374705948, 111.95386380445362), new DecimalPosition(101.94873938255641, 109.76929661959905), new DecimalPosition(97.40030501805334, 107.58472943474447), new DecimalPosition(92.85187065355028, 105.4001622498899), new DecimalPosition(88.30343628904721, 103.21559506503533), new DecimalPosition(85.10232369337074, 102.43126936363056), new DecimalPosition(81.85666976052757, 103.00399897199927), new DecimalPosition(79.11733745764722, 104.83657835376664), new DecimalPosition(77.34925519408627, 107.61797616158984)));
        Polygon2D slopePolygon = new Polygon2D(Arrays.asList(new DecimalPosition(68.9312990931717, 141.45786409111875), new DecimalPosition(70.66890949477038, 136.82423635352228), new DecimalPosition(72.40651989636905, 132.19060861592578), new DecimalPosition(74.14413029796773, 127.55698087832931), new DecimalPosition(75.88174069956641, 122.92335314073284), new DecimalPosition(77.61935110116508, 118.28972540313636), new DecimalPosition(79.35696150276377, 113.65609766553987), new DecimalPosition(81.09457190436244, 109.0224699279434), new DecimalPosition(81.97861303614292, 107.6317710240318), new DecimalPosition(83.34827918758309, 106.71548133314812), new DecimalPosition(84.97110615400469, 106.42911652896377), new DecimalPosition(86.57166245184291, 106.82127937966615), new DecimalPosition(91.12009681634598, 109.00584656452072), new DecimalPosition(95.66853118084904, 111.1904137493753), new DecimalPosition(100.21696554535211, 113.37498093422987), new DecimalPosition(104.76539990985518, 115.55954811908444), new DecimalPosition(109.31383427435824, 117.74411530393901), new DecimalPosition(113.86226863886131, 119.92868248879358), new DecimalPosition(118.41070300336438, 122.11324967364816), new DecimalPosition(122.95913736786744, 124.29781685850273), new DecimalPosition(127.50757173237051, 126.48238404335729), new DecimalPosition(132.05600609687357, 128.66695122821187), new DecimalPosition(136.60444046137664, 130.85151841306643), new DecimalPosition(141.1528748258797, 133.03608559792102), new DecimalPosition(145.70130919038277, 135.22065278277557), new DecimalPosition(150.24974355488584, 137.40521996763016), new DecimalPosition(154.79817791938893, 139.58978715248472), new DecimalPosition(159.34661228389197, 141.7743543373393), new DecimalPosition(163.89504664839504, 143.95892152219386), new DecimalPosition(165.39749396567507, 145.2109249910246), new DecimalPosition(166.12678846087164, 147.0255847846063), new DecimalPosition(165.90858952146692, 148.96909960317825), new DecimalPosition(164.17097911986824, 153.60272734077472), new DecimalPosition(162.43336871826958, 158.2363550783712), new DecimalPosition(160.6957583166709, 162.8699828159677), new DecimalPosition(158.9581479150722, 167.50361055356416), new DecimalPosition(157.22053751347352, 172.13723829116063), new DecimalPosition(155.48292711187486, 176.7708660287571), new DecimalPosition(153.74531671027617, 181.40449376635357)));
        Polygon2D growthSlopePolygon = new Polygon2D(Arrays.asList(new DecimalPosition(69.07093148881707, 141.130584020268), new DecimalPosition(153.60568431463082, 181.7317738372043), new DecimalPosition(165.76895712582157, 149.29637967402897), new DecimalPosition(81.2342043000078, 108.69518985709269)));
        Polygon2D growthFlatPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(84.83988861463862, 110.42696369429697), new DecimalPosition(89.38832297914169, 112.61153087915154), new DecimalPosition(93.93675734364476, 114.79609806400612), new DecimalPosition(98.48519170814782, 116.98066524886069), new DecimalPosition(103.03362607265089, 119.16523243371526), new DecimalPosition(107.58206043715396, 121.34979961856983), new DecimalPosition(112.13049480165702, 123.5343668034244), new DecimalPosition(116.67892916616009, 125.71893398827898), new DecimalPosition(121.22736353066315, 127.90350117313355), new DecimalPosition(125.77579789516622, 130.0880683579881), new DecimalPosition(130.3242322596693, 132.2726355428427), new DecimalPosition(134.87266662417235, 134.45720272769725), new DecimalPosition(139.42110098867542, 136.64176991255184), new DecimalPosition(143.9695353531785, 138.8263370974064), new DecimalPosition(148.51796971768155, 141.01090428226098), new DecimalPosition(153.06640408218465, 143.19547146711554), new DecimalPosition(157.61483844668768, 145.3800386519701), new DecimalPosition(162.16327281119075, 147.56460583682468), new DecimalPosition(172.46289376445023, 151.426963694297), new DecimalPosition(173.06294084781322, 146.08229794322415), new DecimalPosition(171.0573809860226, 141.09198351087443), new DecimalPosition(166.92565086350254, 137.64897397158992), new DecimalPosition(162.37721649899947, 135.46440678673537), new DecimalPosition(157.8287821344964, 133.27983960188078), new DecimalPosition(153.28034776999334, 131.09527241702622), new DecimalPosition(148.73191340549027, 128.91070523217164), new DecimalPosition(144.1834790409872, 126.72613804731706), new DecimalPosition(139.63504467648414, 124.54157086246249), new DecimalPosition(135.08661031198108, 122.35700367760793), new DecimalPosition(130.538175947478, 120.17243649275335), new DecimalPosition(125.98974158297494, 117.98786930789879), new DecimalPosition(121.44130721847189, 115.80330212304422), new DecimalPosition(116.89287285396883, 113.61873493818965), new DecimalPosition(112.34443848946574, 111.43416775333507), new DecimalPosition(107.79600412496269, 109.2496005684805), new DecimalPosition(103.24756976045963, 107.06503338362593), new DecimalPosition(98.69913539595656, 104.88046619877136), new DecimalPosition(94.1507010314535, 102.69589901391679), new DecimalPosition(89.60226666695043, 100.51133182906221), new DecimalPosition(85.20073684789529, 99.43288398963067), new DecimalPosition(80.73796269023592, 100.22038720113763), new DecimalPosition(76.97138077377545, 102.74018385106777), new DecimalPosition(74.54026766137913, 106.56460583682467)));    //----------------------------------------------------------------------------
        showDisplay(new PositionMarker()/*.addPolygon2D(flatPolygon).addPolygon2D(slopePolygon)*/.addPolygon2D(growthSlopePolygon).addPolygon2D(growthFlatPolygon));
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayCorner2.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    @Test
    public void testSlope2DrivewaysShape() {
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(30, 40, null), GameTestHelper.createTerrainSlopeCorner(160, 40, null),
                GameTestHelper.createTerrainSlopeCorner(160, 60, 1), GameTestHelper.createTerrainSlopeCorner(160, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(160, 120, null), GameTestHelper.createTerrainSlopeCorner(110, 120, 1), GameTestHelper.createTerrainSlopeCorner(70, 120, 1), GameTestHelper.createTerrainSlopeCorner(30, 120, null));
        // AssertTerrainShape.saveTerrainShape( terrainShape, "testSlopeDrivewayShape1.json");
        showDisplay();
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSlope2DrivewaysShape.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    private Collection<TerrainTile> setup(TerrainSlopeCorner... slopePolygon) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 4, 1),},
                {GameTestHelper.createSlopeNode(4, 8, 0.7),},
                {GameTestHelper.createSlopeNode(7, 12, 0.7),},
                {GameTestHelper.createSlopeNode(10, 16, 0.7),},
                {GameTestHelper.createSlopeNode(11, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setOuterLineGameEngine(3).setInnerLineGameEngine(7);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(slopePolygon));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3}
        };

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 2)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 2)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(2, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(2, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(2, 2)));

        return terrainTiles;
    }
}
