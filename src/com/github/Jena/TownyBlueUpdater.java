package com.github.Jena;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

public class TownyBlueUpdater {
    //todo add config values
    public static Color towncolor = new Color(255, 0, 0, 100);
    public static Color nationcolor = new Color(13, 255, 235, 100);
    private static final FileConfiguration config = TownyBlue.config;
    protected static String space = "_";


    public static void CompleteUpdate() {
        BlueMapAPI.getInstance().ifPresent(blueMapAPI -> {
            try {
                // create the MarkerSet
                if (blueMapAPI.getMarkerAPI().getMarkerSet("towns").isPresent()) {
                    blueMapAPI.getMarkerAPI().removeMarkerSet("towns");
                }
                MarkerSet set = blueMapAPI.getMarkerAPI().createMarkerSet("towns");

                // update markers
                for (BlueMapMap map : blueMapAPI.getMaps()) {
                    if (TownyAPI.getInstance().getTownyWorld(map.getName()) != null) {
                        TownyWorld world = TownyAPI.getInstance().getTownyWorld(map.getName());

                        for (TownBlock townBlock : world.getTownBlocks()) {
                            if (townBlock.hasTown()) {
                                Town town = townBlock.getTown();
                                double x = townBlock.getX() * 16;
                                double z = townBlock.getZ() * 16;
                                double y = config.getDouble("height");
                                Vector2d vector2a = new Vector2d(x, z);
                                Vector2d vector2b = new Vector2d(x + 16, z + 16);

                                // making the basic shape marker and assigning default values
                                Shape shape = new Shape(vector2a, vector2b);
                                ShapeMarker marker = set.createShapeMarker(town.getName() + space + x + space + z, map, shape, (float) y);
                                marker.setLabel(getHTMLforTown(town));
                                marker.setMaxDistance(1500);

                                // this is making different colors for towns in a nation
                                if (town.hasNation()) {
                                    marker.setFillColor(nationcolor);
                                } else {marker.setFillColor(towncolor);}
                                marker.setBorderColor(marker.getFillColor());

                                // adding homeblock marker
                                if (townBlock.isHomeBlock()) {
                                    Vector3d vector3d = new Vector3d(x + 8, y + 2, z + 8);
                                    POIMarker poiMarker = set.createPOIMarker(town.getName() + space + "icon", map, vector3d);
                                    poiMarker.setLabel(getHTMLforTown(townBlock.getTown()));
                                    if (town.isCapital()) {
                                        poiMarker.setIcon(TownyBlue.config.getString("capital-marker"), poiMarker.getIconAnchor());
                                    } else {poiMarker.setIcon(TownyBlue.config.getString("home-marker"), poiMarker.getIconAnchor());}
                                    poiMarker.setMaxDistance(TownyBlue.config.getInt("max-distance"));
                                }
                            } else {return;}
                        }
                    }
                }
                blueMapAPI.getMarkerAPI().save();
            } catch (IOException | NotRegisteredException e) {e.printStackTrace();}
        });
    }

    public static void UpdateTown(Town town) {
        BlueMapAPI.getInstance().ifPresent(blueMapAPI -> {
            try {
                // get the MarkerSet
                MarkerSet set;
                if (blueMapAPI.getMarkerAPI().getMarkerSet("towns").isPresent()) {
                    set = blueMapAPI.getMarkerAPI().getMarkerSet("towns").get();
                } else {set = blueMapAPI.getMarkerAPI().createMarkerSet("towns");}

                // get the map
                BlueMapMap map = null;
                for (BlueMapMap mapMap : blueMapAPI.getMaps()) {
                    if (town.getWorld().getUID() == mapMap.getWorld().getUuid()) {
                        map = mapMap;
                    }
                }
                if (map != null) {
                    for (TownBlock townBlock : town.getTownBlocks()) {
                        double x = townBlock.getX() * 16;
                        double z = townBlock.getZ() * 16;
                        Vector2d vector2a = new Vector2d(x, z);
                        Vector2d vector2b = new Vector2d(x + 16, z + 16);
                        double y = config.getDouble("height");
                        String id = town.getName() + space + x + space + z;
                        Optional<Marker> marker = set.getMarker(id);


                        if (marker.isPresent()) {
                            marker.get().setLabel(getHTMLforTown(town));

                            // if it's homeblock
                            if (townBlock.isHomeBlock()) {
                                Vector3d vector3d = new Vector3d(x + 8, y + 2, z + 8);
                                if (set.getMarker(town.getName() + space + "icon").isPresent()) {
                                    set.removeMarker(town.getName() + space + "icon");
                                }
                                POIMarker poiMarker = set.createPOIMarker(town.getName() + space + "icon", map, vector3d);
                                poiMarker.setLabel(getHTMLforTown(townBlock.getTown()));
                                if (town.isCapital()) {
                                    poiMarker.setIcon(TownyBlue.config.getString("capital-marker"), poiMarker.getIconAnchor());
                                } else {
                                    poiMarker.setIcon(TownyBlue.config.getString("home-marker"), poiMarker.getIconAnchor());
                                }
                                poiMarker.setMaxDistance(TownyBlue.config.getInt("max-distance"));
                            }
                        } else {
                            // same shape stuff as complete update
                            Shape shape = new Shape(vector2a, vector2b);
                            ShapeMarker marker1 = set.createShapeMarker(town.getName() + space + x + space + z, map, shape, (float) y);
                            marker1.setLabel(getHTMLforTown(town));
                            marker1.setMaxDistance(1500);

                            // this is making different colors for towns in a nation
                            if (town.hasNation()) {
                                marker1.setFillColor(nationcolor);
                            } else {
                                marker1.setFillColor(towncolor);
                            }
                            marker1.setBorderColor(marker1.getFillColor());

                            // adding homeblock marker
                            if (townBlock.isHomeBlock()) {
                                Vector3d vector3d = new Vector3d(x + 8, y + 2, z + 8);
                                POIMarker poiMarker = set.createPOIMarker(town.getName() + space + "icon", map, vector3d);
                                poiMarker.setLabel(getHTMLforTown(townBlock.getTown()));
                                if (town.isCapital()) {
                                    poiMarker.setIcon(TownyBlue.config.getString("capital-marker"), poiMarker.getIconAnchor());
                                } else {
                                    poiMarker.setIcon(TownyBlue.config.getString("home-marker"), poiMarker.getIconAnchor());
                                }
                                poiMarker.setMaxDistance(TownyBlue.config.getInt("max-distance"));
                            }
                        }
                    }

                    // for removing unclaimed plots
                    for (Marker marker : set.getMarkers()) {
                        if (marker.getId().contains(town.getName()) && !marker.getId().contains("icon")) {
                            String s = marker.getId().replace(town.getName() + space, "");
                            String[] strings =  s.split(space);

                            if (strings.length == 2) {
                                int x = Integer.parseInt(strings[0]);
                                int z = Integer.parseInt(strings[1]);
                                if (TownyAPI.getInstance().isWilderness(town.getWorld().getBlockAt(x * 16, 0, z * 16).getLocation())) {
                                    set.removeMarker(marker);
                                } else {
                                    TownBlock townBlock = TownyAPI.getInstance().getTownBlock(town.getWorld().getBlockAt(x * 16, 0, z * 16).getLocation());
                                    if (townBlock.getTown() != town) {
                                        set.removeMarker(marker);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException | NotRegisteredException e) {e.printStackTrace();}
        });
    }

    public static void UpdatePlot(TownBlock townBlock) {
        BlueMapAPI.getInstance().ifPresent(blueMapAPI -> {
            try {
                // get the MarkerSet
                MarkerSet set;
                if (blueMapAPI.getMarkerAPI().getMarkerSet("towns").isPresent()) {
                    set = blueMapAPI.getMarkerAPI().getMarkerSet("towns").get();
                } else {
                    set = blueMapAPI.getMarkerAPI().createMarkerSet("towns");
                }

                // get the map
                BlueMapMap map = null;
                for (BlueMapMap mapMap : blueMapAPI.getMaps()) {
                    Town exampletown = townBlock.getWorld().getTowns().values().iterator().next();
                    if (exampletown.getWorld().getUID() == mapMap.getWorld().getUuid()) {
                        map = mapMap;
                    }
                }

                if (townBlock.hasTown()) {
                    for (Marker marker : set.getMarkers()) {
                        if (marker.getId().contains(String.valueOf(townBlock.getX())) && marker.getId().contains(String.valueOf(townBlock.getZ()))) {
                            String s = marker.getId();
                            s = s.replace(space + townBlock.getX() + space + townBlock.getZ(), "");
                            try {
                                Town town1 = TownyUniverse.getInstance().getDataSource().getTown(s);
                                if (townBlock.getTown() == town1) {
                                    double x = townBlock.getX() * 16;
                                    double z = townBlock.getZ() * 16;
                                    Vector2d vector2a = new Vector2d(x, z);
                                    Vector2d vector2b = new Vector2d(x + 16, z + 16);
                                    double y = config.getDouble("height");

                                    // adding shapemarker
                                    set.removeMarker(marker);
                                    Shape shape = new Shape(vector2a, vector2b);
                                    ShapeMarker marker1 = set.createShapeMarker(town1.getName() + space + x + space + z, map, shape, (float) y);
                                    marker.setLabel(getHTMLforTown(town1));
                                    marker.setMaxDistance(1500);

                                    // this is making different colors for towns in a nation
                                    if (town1.hasNation()) {
                                        marker1.setFillColor(nationcolor);
                                    } else {
                                        marker1.setFillColor(towncolor);
                                    }
                                    marker1.setBorderColor(marker1.getFillColor());

                                    // adding homeblock marker
                                    if (townBlock.isHomeBlock()) {
                                        Vector3d vector3d = new Vector3d(x + 8, y + 2, z + 8);
                                        POIMarker poiMarker = set.createPOIMarker(town1.getName() + space + "icon", map, vector3d);
                                        poiMarker.setLabel(getHTMLforTown(town1));
                                        if (town1.isCapital()) {
                                            poiMarker.setIcon(TownyBlue.config.getString("capital-marker"), poiMarker.getIconAnchor());
                                        } else {
                                            poiMarker.setIcon(TownyBlue.config.getString("home-marker"), poiMarker.getIconAnchor());
                                        }
                                        poiMarker.setMaxDistance(TownyBlue.config.getInt("max-distance"));
                                    }
                                } else {
                                    Town town = townBlock.getTown();

                                    double x = townBlock.getX() * 16;
                                    double z = townBlock.getZ() * 16;
                                    Vector2d vector2a = new Vector2d(x, z);
                                    Vector2d vector2b = new Vector2d(x + 16, z + 16);
                                    double y = config.getDouble("height");

                                    // adding shapemarker
                                    set.removeMarker(marker);
                                    Shape shape = new Shape(vector2a, vector2b);
                                    ShapeMarker marker1 = set.createShapeMarker(town.getName() + space + x + space + z, map, shape, (float) y);
                                    marker.setLabel(getHTMLforTown(town));
                                    marker.setMaxDistance(1500);

                                    // this is making different colors for towns in a nation
                                    if (town.hasNation()) {
                                        marker1.setFillColor(nationcolor);
                                    } else {
                                        marker1.setFillColor(towncolor);
                                    }
                                    marker1.setBorderColor(marker1.getFillColor());

                                    // adding homeblock marker
                                    if (townBlock.isHomeBlock()) {
                                        Vector3d vector3d = new Vector3d(x + 8, y + 2, z + 8);
                                        POIMarker poiMarker = set.createPOIMarker(town.getName() + space + "icon", map, vector3d);
                                        poiMarker.setLabel(getHTMLforTown(town));
                                        if (town.isCapital()) {
                                            poiMarker.setIcon(TownyBlue.config.getString("capital-marker"), poiMarker.getIconAnchor());
                                        } else {
                                            poiMarker.setIcon(TownyBlue.config.getString("home-marker"), poiMarker.getIconAnchor());
                                        }
                                        poiMarker.setMaxDistance(TownyBlue.config.getInt("max-distance"));
                                    }
                                }
                            } catch (NotRegisteredException e) {
                                e.printStackTrace();
                                try {
                                    Town town = townBlock.getTown();

                                    double x = townBlock.getX() * 16;
                                    double z = townBlock.getZ() * 16;
                                    Vector2d vector2a = new Vector2d(x, z);
                                    Vector2d vector2b = new Vector2d(x + 16, z + 16);
                                    double y = config.getDouble("height");

                                    // adding shapemarker
                                    set.removeMarker(marker);
                                    Shape shape = new Shape(vector2a, vector2b);
                                    ShapeMarker marker1 = set.createShapeMarker(town.getName() + space + x + space + z, map, shape, (float) y);
                                    marker.setLabel(getHTMLforTown(town));
                                    marker.setMaxDistance(1500);

                                    // this is making different colors for towns in a nation
                                    if (town.hasNation()) {
                                        marker1.setFillColor(nationcolor);
                                    } else {
                                        marker1.setFillColor(towncolor);
                                    }
                                    marker1.setBorderColor(marker1.getFillColor());

                                    // adding homeblock marker
                                    if (townBlock.isHomeBlock()) {
                                        Vector3d vector3d = new Vector3d(x + 8, y + 2, z + 8);
                                        POIMarker poiMarker = set.createPOIMarker(town.getName() + space + "icon", map, vector3d);
                                        poiMarker.setLabel(getHTMLforTown(town));
                                        if (town.isCapital()) {
                                            poiMarker.setIcon(TownyBlue.config.getString("capital-marker"), poiMarker.getIconAnchor());
                                        } else {
                                            poiMarker.setIcon(TownyBlue.config.getString("home-marker"), poiMarker.getIconAnchor());
                                        }
                                        poiMarker.setMaxDistance(TownyBlue.config.getInt("max-distance"));
                                    }
                                } catch (NotRegisteredException ee) {
                                    ee.printStackTrace();
                                    set.removeMarker(marker);
                                }
                            }
                        }
                    }
                } else {
                    for (Marker marker : set.getMarkers()) {
                        if (marker.getId().contains(String.valueOf(townBlock.getX())) && marker.getId().contains(String.valueOf(townBlock.getZ()))) {
                            set.removeMarker(marker);
                        }
                    }
                }
            } catch (IOException e) {e.printStackTrace();}
        });
    }

    public static String getHTMLforTown(Town town) {
        String Html = TownyBlue.config.getString("html");
        if (Html != null) {
            String stringresidents = getResidents(town);

            // placeholders
            Html = Html.replace("%name%", town.getName());
            if (town.hasNation()) {
                try {
                    Html = Html.replace("%nation%", town.getNation().getName());
                } catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
            } else {
                Html = Html.replace("%nation%", "");
            }
            Html = Html.replace("%mayor%", town.getMayor().getName());
            Html = Html.replace("%residents%", stringresidents);
            Html = Html.replace("%residentcount%", String.valueOf(town.getResidents().toArray().length));
            // add extra placeholders here, just Html = Html.replace("%yourplaceholder%", thing);

        } else {Html = "";}

        return Html;
    }

    private static String getResidents(Town town) {
        String result = "";
        StringBuilder resultBuilder = new StringBuilder(result);
        resultBuilder.append(town.getMayor().getName());

        for (Resident resident : town.getResidents()) {
            if (resident.getName() != null) {
                if (!resident.isMayor()) {
                    resultBuilder.append(", ");
                    resultBuilder.append(resident.getName());
                }
            }
        }
        result = resultBuilder.toString();

        return result;
    }
}
/*
*
* */
