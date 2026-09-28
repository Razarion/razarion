package com.btxtech.server.service.ui;

import com.btxtech.server.service.engine.PlanetCrudService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Noob Island cut out of the starter planet's minimap, for the loading screen (2026-09-28).
 * <p>
 * The page shows it while the game starts, before it knows anything about the player - the same
 * reasoning as the terrain prefetch, which asks for the starter planet too. The whole minimap is
 * 300 KB and would compete with the start for bandwidth; the island is a few dozen.
 * <p>
 * Cut once per minimap image and kept: the source changes only when the editor saves a new one,
 * and its stored digest says when that happened.
 */
@Service
public class StarterMiniMapService {
    /**
     * World metres of the square cut from the planet's lower left corner. Noob Island is the
     * Phase 1 polygon of docs/game-design/progression.md, reaching 810 m east and 756 m north; a
     * little water around it frames it. The loading screen's zoom target in index.html is given
     * as a share of this square, so the two change together.
     */
    static final double NOOB_CROP_METRES = 920;
    private static final long PLANET_TTL_MILLIS = 60_000;
    private final GameUiContextService gameUiContextService;
    private final PlanetCrudService planetCrudService;
    private volatile StarterPlanet starterPlanet;
    private volatile Crop crop;

    public StarterMiniMapService(GameUiContextService gameUiContextService, PlanetCrudService planetCrudService) {
        this.gameUiContextService = gameUiContextService;
        this.planetCrudService = planetCrudService;
    }

    /** The entity tag of the crop, or null if the starter planet has no minimap. Reads the digest column only. */
    public String getNoobDigest() {
        String digest = planetCrudService.getMiniMapDigest(starterPlanet().planetId());
        return digest != null ? digest + "-noob" : null;
    }

    /** The PNG, or null if the starter planet has no minimap. */
    public byte[] getNoobImage() {
        StarterPlanet planet = starterPlanet();
        String digest = planetCrudService.getMiniMapDigest(planet.planetId());
        if (digest == null) {
            return null;
        }
        Crop current = crop;
        if (current == null || !current.sourceDigest().equals(digest)) {
            byte[] source = planetCrudService.getMiniMapImage(planet.planetId());
            current = new Crop(digest, cut(source, planet.sizeX()));
            crop = current;
        }
        return current.png();
    }

    /**
     * The square [0, NOOB_CROP_METRES] in both axes. The image runs top to bottom where the world
     * runs south to north, so the planet's lower left corner is the image's lower left.
     */
    static byte[] cut(byte[] source, double planetSizeX) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(source));
            int side = (int) Math.min(Math.round(NOOB_CROP_METRES * image.getWidth() / planetSizeX),
                    Math.min(image.getWidth(), image.getHeight()));
            BufferedImage cut = image.getSubimage(0, image.getHeight() - side, side, side);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(cut, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private StarterPlanet starterPlanet() {
        StarterPlanet planet = starterPlanet;
        long now = System.currentTimeMillis();
        if (planet == null || now - planet.loadedAt() > PLANET_TTL_MILLIS) {
            PlanetConfig planetConfig = gameUiContextService.starterPlanetConfig();
            planet = new StarterPlanet(planetConfig.getId(), planetConfig.getSize().getX(), now);
            starterPlanet = planet;
        }
        return planet;
    }

    private record StarterPlanet(int planetId, double sizeX, long loadedAt) {
    }

    private record Crop(String sourceDigest, byte[] png) {
    }
}
