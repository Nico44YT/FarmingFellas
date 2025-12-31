package nico.farmingfellas.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Zone {
    private Identifier worldId;

    private int zoneId;
    private int zoneColor;

    private BlockPos cornerA;
    private BlockPos cornerB;

    private final List<BlockPos> chestPositions;

    public Zone(Identifier worldId, int zoneId, int zoneColor, BlockPos cornerA, BlockPos cornerB, BlockPos[] chestPositions) {
        this.worldId = worldId;
        this.zoneId = zoneId;
        this.zoneColor = zoneColor;

        this.cornerA = cornerA;
        this.cornerB = cornerB;

        this.chestPositions = new ArrayList<>(List.of(chestPositions));
    }

    public void writeNbt(NbtCompound nbtCompound) {
        nbtCompound.putString("worldId", worldId.toString());

        nbtCompound.putInt("zoneId", zoneId);
        nbtCompound.putInt("zoneColor", zoneColor);

        nbtCompound.putLong("corner_a", cornerA.asLong());
        nbtCompound.putLong("corner_b", cornerB.asLong());

        nbtCompound.putLongArray("chest_positions", chestPositions.stream().map(BlockPos::asLong).toList());
    }

    public void readNbt(NbtCompound nbtCompound) {
        this.worldId = Identifier.tryParse(nbtCompound.getString("worldId"));

        this.zoneId = nbtCompound.getInt("zoneId");
        this.zoneColor = nbtCompound.getInt("zoneColor");

        this.cornerA = BlockPos.fromLong(nbtCompound.getLong("corner_a"));
        this.cornerB = BlockPos.fromLong(nbtCompound.getLong("corner_b"));

        this.chestPositions.clear();
        for (long l : nbtCompound.getLongArray("chest_positions")) {
            this.chestPositions.add(BlockPos.fromLong(l));
        }
    }

    public JsonElement toJsonObject() {
        JsonObject object = new JsonObject();

        object.addProperty("worldId", this.worldId.toString());
        object.addProperty("zoneId", this.zoneId);
        object.addProperty("zoneColor", this.zoneColor);
        object.addProperty("cornerA", this.cornerA.asLong());
        object.addProperty("cornerB", this.cornerB.asLong());
        JsonArray array = new JsonArray();
        this.chestPositions.forEach(blockPos -> {
            array.add(blockPos.asLong());
        });
        object.add("chestPositions", array);

        return object;
    }

    public static Zone fromJson(JsonObject jsonObject) {
        Identifier worldId = Identifier.tryParse(jsonObject.get("worldId").getAsString());

        int zoneId = jsonObject.get("zoneId").getAsInt();
        int zoneColor = jsonObject.get("zoneColor").getAsInt();

        BlockPos cornerA = BlockPos.fromLong(jsonObject.get("cornerA").getAsLong());
        BlockPos cornerB = BlockPos.fromLong(jsonObject.get("cornerB").getAsLong());
        List<BlockPos> chestPositions = new ArrayList<>();
        JsonArray array = jsonObject.get("chestPositions").getAsJsonArray();
        for (JsonElement element : array) {
            chestPositions.add(BlockPos.fromLong(element.getAsLong()));
        }

        return new Zone(
                worldId,
                zoneId, zoneColor,
                cornerA, cornerB,
                chestPositions.toArray(BlockPos[]::new)
        );
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Zone other && other.zoneId == this.zoneId;
    }

    @Override
    public int hashCode() {
        return this.zoneId * 31;
    }
}
