package nico.farmingfellas.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import nico.farmingfellas.FarmingFellasMain;

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

    private final Set<Zone> zoneList;

    public ZoneManager() {
        this.zoneList = new HashSet<>();
    }

    public void addZone(Zone zone) {
        this.zoneList.add(zone);
    }

    public void removeZone(Zone zone) {
        this.zoneList.remove(zone);
    }

    public JsonElement toJson() {
        JsonArray arr = new JsonArray();

        zoneList.forEach(zone -> {
            arr.add(zone.toJsonObject());
        });

        return arr;
    }

    public void fromJson(JsonElement json) {
        if (!json.isJsonArray()) throw new RuntimeException("Malformed JSON");

        this.zoneList.clear();
        JsonArray arr = json.getAsJsonArray();

        arr.forEach(element -> {
            this.zoneList.add(Zone.fromJson(element.getAsJsonObject()));
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
            System.out.println("UNLOADED");

            getInstance().addZone(new Zone(ServerWorld.OVERWORLD.getValue(), 0, 0xFFFFFFFF, new BlockPos(0, 0, 0), new BlockPos(5, 5, 5), new BlockPos[0]));
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
