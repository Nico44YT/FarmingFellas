package nico.farmingfellas.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.FarmingFellasUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ZoneManager {
    //region // * Singleton * //
    private static final ZoneManager INSTANCE = new ZoneManager();

    public static ZoneManager getInstance() {
        return INSTANCE;
    }
    //endregion

    public static final WorldSavePath FARMING_FELLAS_DATA = new WorldSavePath("data/" + FarmingFellasMain.MOD_ID + "/");

    private final Set<Zone> zoneSet;

    public ZoneManager() {
        this.zoneSet = new HashSet<>();
    }

    public Zone getZone(int id) {
        return this.zoneSet.stream().filter(zone -> zone.getZoneId() == id).findFirst().orElse(null);
    }

    public void addZone(Zone zone) {
        this.zoneSet.add(zone);
    }

    public void removeZone(Zone zone) {
        this.zoneSet.remove(zone);
    }

    public Zone createNewZone(World world) {
        int zoneColor = FarmingFellasUtil.randomColor(world.getRandom());
        Zone newZone = new Zone(world.getRegistryKey().getValue(), zoneSet.size(), zoneColor, null, null, new BlockPos[0]);
        addZone(newZone);
        return newZone;
    }

    public JsonElement toJson() {
        JsonArray arr = new JsonArray();

        zoneSet.forEach(zone -> {
            arr.add(zone.toJsonObject());
        });

        return arr;
    }

    public void fromJson(JsonElement json) {
        if (!json.isJsonArray()) throw new RuntimeException("Malformed JSON");

        this.zoneSet.clear();
        JsonArray arr = json.getAsJsonArray();

        arr.forEach(element -> {
            this.zoneSet.add(Zone.fromJson(element.getAsJsonObject()));
        });
    }

    public static void onWorldLoad(MinecraftServer server, ServerWorld world) {
        try {
            Path path = server.getSavePath(ZoneManager.FARMING_FELLAS_DATA);
            File dataFolder = path.toFile();

            if (!dataFolder.exists()) dataFolder.mkdirs();

            File zonesFile = new File(dataFolder, "zones.json");
            if (!zonesFile.exists()) return;

            FileReader reader = new FileReader(zonesFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            getInstance().fromJson(gson.fromJson(reader, JsonArray.class));
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onWorldUnload(MinecraftServer server, ServerWorld world) {
        try {
            Path path = server.getSavePath(ZoneManager.FARMING_FELLAS_DATA);
            File dataFolder = path.toFile();

            if (!dataFolder.exists()) dataFolder.mkdirs();

            File zonesFile = new File(dataFolder, "zones.json");

            FileWriter writer = new FileWriter(zonesFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(getInstance().toJson(), writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
