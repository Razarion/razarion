package com.btxtech.gameengine.pathing;

import com.btxtech.GameMock;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line2I;
import com.btxtech.shared.gameengine.pathing.Obstacle;
import com.btxtech.shared.gameengine.pathing.Pathing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Scenario {
    private Pathing pathing;
    private AtomicInteger idGenerator = new AtomicInteger(1);
    private List<Runnable> scenes = new ArrayList<>();
    public int number;
    private BackgroundWorker backgroundWorker;

    public Scenario() {
        setup();
    }

    public Pathing init(int number) {
        if (backgroundWorker != null) {
            backgroundWorker.stop();
            backgroundWorker = null;
        }
        pathing = new Pathing();
        idGenerator.set(1);
        Runnable runnable = scenes.get(number);
        runnable.run();
        this.number = number;
        System.out.println("Scenario: " + number);
        // Clean up afterwards
        Pathing tmpPathing = pathing;
        pathing = null;
        if (backgroundWorker != null) {
            backgroundWorker.start();
        }
        return tmpPathing;
    }

    public Pathing initNext() {
        int nextNumber = ++number;
        if (nextNumber > scenes.size() - 1) {
            nextNumber = 0;
        }
        return init(nextNumber);
    }

    public Pathing initPrevious() {
        int nextNumber = --number;
        if (nextNumber < 0) {
            nextNumber = scenes.size() - 1;
        }
        return init(nextNumber);
    }

    public Pathing initCurrent() {
        return init(number);
    }

    public int getNumber() {
        return number;
    }

    private void createAndUnit(boolean canMove, double radius, DecimalPosition position, DecimalPosition destination) {
        pathing.createUnit(idGenerator.getAndIncrement(), canMove, radius, position, destination, null);
    }

    private void createAndRectangleObstacle(int x, int y, int width, int height) {
        createAndObstacle(new Line2I(new Index(x, y), new Index(x + width, y)));
        createAndObstacle(new Line2I(new Index(x + width, y), new Index(x + width, y + height)));
        createAndObstacle(new Line2I(new Index(x + width, y + height), new Index(x, y + height)));
        createAndObstacle(new Line2I(new Index(x, y + height), new Index(x, y)));
    }

    private void createAndObstacle(Line2I line) {
        pathing.createObstacle(line);
    }

    private void setup() {
        // Simple move
        //0
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(0, 0), new DecimalPosition(-100, 0));
            }
        });
        // Stop condition
        //1
        scenes.add(new Runnable() {
            @Override

            public void run() {
                DecimalPosition destination = new DecimalPosition(0, 0);
                createAndUnit(true, 10, new DecimalPosition(-100, 0), destination);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), destination);
                    }
                }
            }
        });
        // 2
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
            }
        });
        // 3
        scenes.add(new Runnable() {
            @Override
            public void run() {
                backgroundWorker = new BackgroundWorker(pathing, new Runnable() {
                    @Override
                    public void run() {
                        backgroundWorker.getPathing().createUnit(idGenerator.getAndIncrement(), true, 10, new DecimalPosition(-200, 0), new DecimalPosition(200, 0), null);
                    }
                }, 1000);
            }
        });
        // 4
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                createAndUnit(true, 10, new DecimalPosition(0, -10), direction);
                createAndUnit(true, 10, new DecimalPosition(0, 10), direction);
            }
        });
        // 5
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(-60, 0), new DecimalPosition(0, 0));
                createAndUnit(true, 10, new DecimalPosition(-20, 0), null);
                createAndUnit(true, 10, new DecimalPosition(0, 0), null);
            }
        });
        // 6
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(0, 0), new DecimalPosition(100, 0));
            }
        });
        // Bypass frontal (bui1dings)
        // 7
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(0, 0), new DecimalPosition(100, 0));
                createAndUnit(false, 10, new DecimalPosition(50, 0), null);
            }
        });
        // 8
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(-30, 0), new DecimalPosition(100, 0));
                createAndUnit(true, 10, new DecimalPosition(0, 0), null);
                createAndUnit(true, 10, new DecimalPosition(20, 0), null);
                createAndUnit(false, 10, new DecimalPosition(40, 0), null);
            }
        });
        // Moving units vs fix units
        // 9
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(0, 0), new DecimalPosition(100, 0));
                createAndUnit(false, 10, new DecimalPosition(30, 0), null);
            }
        });
        // 10
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(0, 5), new DecimalPosition(100, 5));
                createAndUnit(false, 10, new DecimalPosition(30, 0), null);
            }
        });
        // Moving against each other
        // 11
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(-30, 5), new DecimalPosition(100, 5));
                createAndUnit(true, 10, new DecimalPosition(30, 0), new DecimalPosition(-100, 0));
            }
        });
        // 12
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(-30, 0), new DecimalPosition(100, 0));
                createAndUnit(true, 10, new DecimalPosition(30, 0), new DecimalPosition(-100, 0));
            }
        });
        // Moving vs standing
        // 13
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(30, 5), null);
            }
        });
        // 14
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(30, 0), null);
            }
        });
        // 15
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(30, 0), null);
                createAndUnit(true, 10, new DecimalPosition(50, 0), null);
            }
        });
        // 16
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndUnit(true, 10, new DecimalPosition(-100, 0), new DecimalPosition(200, 0));
                // createAndUnit(true, 10, new DecimalPosition(-100, 10), new DecimalPosition(200, 10));
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), null);
                    }
                }
            }
        });
        // 17
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
                createAndUnit(true, 10, new DecimalPosition(100, 0), null);

            }
        });
        // 18
        scenes.add(new Runnable() {
            @Override
            public void run() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x + 100, 20 * y), null);
                    }
                }
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x - 50, 20 * y), direction);
                    }
                }
            }
        });
        // Move to same position
        // 19
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createAndUnit(true, 10, new DecimalPosition(-30, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(30, 0), direction);
            }
        });
        // 20
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(20, 0), direction);
            }
        });
        // 21
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(20, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(-20, 0), direction);
            }
        });
        // 22
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createAndUnit(true, 10, new DecimalPosition(30, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(10, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(-10, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(-30, 0), direction);
            }
        });
        // 23
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createAndUnit(true, 10, new DecimalPosition(-80, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(-60, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(-40, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(-20, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(20, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(40, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(60, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(80, 0), direction);
            }
        });
        // 24
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(30, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 10), direction);
                createAndUnit(true, 10, new DecimalPosition(0, -10), direction);
            }
        });
        // 25
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
            }
        });
        // Overlapping
        // 26
        scenes.add(new Runnable() {
            @Override
            public void run() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(10, 0), direction);
            }
        });
        // Obstacle
        // 27
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndRectangleObstacle(40, -50, 20, 100);
                DecimalPosition direction = new DecimalPosition(100, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
            }
        });
        // 28
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndRectangleObstacle(40, 5, 20, 100);
                DecimalPosition direction = new DecimalPosition(100, 0);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
            }
        });
        // 29
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndRectangleObstacle(40, -50, 20, 100);
                DecimalPosition direction = new DecimalPosition(100, 0);
                // createAndUnit(true, 10, new DecimalPosition(-100, 0), direction);
                // createAndUnit(true, 10, new DecimalPosition(-80, 0), direction);
                // createAndUnit(true, 10, new DecimalPosition(-60, 0), direction);
                // createAndUnit(true, 10, new DecimalPosition(-40, 0), direction);
                // createAndUnit(true, 10, new DecimalPosition(-20, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(0, 0), direction);
                createAndUnit(true, 10, new DecimalPosition(20, 0), direction);
            }
        });
        // 30
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndRectangleObstacle(100, -50, 20, 100);
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
            }
        });
        // 31
        scenes.add(new Runnable() {
            @Override
            public void run() {
                createAndRectangleObstacle(100, 20, 20, 200);
                createAndRectangleObstacle(100, -220, 20, 200);
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
            }
        });
        // Move singie unit out of group
        // 32
        scenes.add(new Runnable() {
            @Override
            public void run() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == 0 && y == 0) {
                            destination = new DecimalPosition(200, 0);
                        }
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), destination);
                    }
                }
            }
        });
        // 33
        scenes.add(new Runnable() {
            @Override
            public void run() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == -2 && y == 0) {
                            destination = new DecimalPosition(200, 0);
                        }
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y), destination);
                    }
                }
            }
        });
        // 34
        scenes.add(new Runnable() {
            @Override
            public void run() {
                // Terrain
                TerrainSurface terrainSurface = GameMock.startTerrainSurface("/SlopeSkeletonSlope.json", "/SlopeSkeletonBeach.json", "/GroundSkeleton.json", "/TerrainSlopePositions.json");
                Collection<Obstacle> obstacles = terrainSurface.getAllObstacles();
                for (Obstacle obstacle : obstacles) {
                    pathing.addObstacle(obstacle);
                }
                // Units
                DecimalPosition destination = null;
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createAndUnit(true, 10, new DecimalPosition(20 * x, 20 * y).add(200, 200), new DecimalPosition(2700, 1700));
                    }
                }

            }
        });
    }

}

