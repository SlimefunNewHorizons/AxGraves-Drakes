package com.artillexstudios.axgraves.grave;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axgraves.AxGraves;
import com.artillexstudios.axgraves.utils.LimitUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.artillexstudios.axgraves.AxGraves.CONFIG;

public class SpawnedGraves {
    private static final ConcurrentLinkedQueue<Grave> graves = new ConcurrentLinkedQueue<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void addGrave(Grave grave) {
        Player player = grave.getPlayer().getPlayer();
        int graveLimit = player == null ? CONFIG.getInt("grave-limit", -1) : LimitUtils.getGraveLimit(player);

        if (graveLimit != -1) {
            int num = 0;
            Grave oldest = grave;

            for (Grave grave2 : graves) {
                if (!grave2.getPlayer().equals(grave.getPlayer())) continue;
                if (oldest.getSpawned() > grave2.getSpawned()) oldest = grave2;
                num++;
            }

            if (num >= graveLimit) oldest.remove();
        }

        graves.add(grave);
    }

    public static void removeGrave(Grave grave) {
        graves.remove(grave);
        persistirTrasCambio();
    }

    /**
     * Vuelca las tumbas a disco en cuanto una desaparece.
     *
     * Quitar la tumba solo la sacaba de la lista en memoria: el fichero seguia teniendo la tumba
     * con sus objetos hasta que saltara el guardado periodico, treinta segundos despues. Si el
     * servidor moria de golpe en esa ventana -- una caida, o el watchdog matando el proceso, que
     * ya ha pasado -- al arrancar se restauraba una tumba ya saqueada y sus objetos aparecian
     * duplicados, porque el jugador se los habia llevado antes. El apagado limpio si guardaba, asi
     * que el fallo solo se veia en cierres forzados y por eso costaba reproducirlo.
     *
     * Se escribe en el ejecutor del plugin para no bloquear el hilo principal, y solo cuando el
     * guardado de tumbas esta activado.
     */
    private static void persistirTrasCambio() {
        if (!AxGraves.CONFIG.getBoolean("save-graves.enabled", true)) return;
        try {
            AxGraves.EXECUTOR.submit(() -> {
                try {
                    saveToFile();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception ignored) {
            // El ejecutor ya esta cerrado (apagado en curso): disable() se encarga de guardar.
        }
    }

    public static ConcurrentLinkedQueue<Grave> getGraves() {
        return graves;
    }

    public static boolean saveToFile() {
        List<SavedGrave> savedGraves = new ArrayList<>();
        for (Grave grave : graves) {
            SavedGrave savedGrave = SavedGrave.create(grave);
            savedGraves.add(savedGrave);
        }

        File file = new File(AxGraves.getInstance().getDataFolder(), "data.json");
        File temporary = new File(AxGraves.getInstance().getDataFolder(), "data.json.tmp");
        try (FileWriter fw = new FileWriter(temporary)) {
            gson.toJson(savedGraves, fw);
            fw.flush();
            try {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            temporary.delete();
            return false;
        }
    }

    public static void loadFromFile() {
        File file = new File(AxGraves.getInstance().getDataFolder(), "data.json");
        if (!file.exists()) return;
        SavedGrave[] savedGraves;
        try (FileReader fw = new FileReader(file)) {
            savedGraves = gson.fromJson(fw, SavedGrave[].class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        file.delete();
        if (savedGraves == null) return;

        for (SavedGrave savedGrave : savedGraves) {
            Grave grave = savedGrave.load();
            if (grave == null) continue;
            addGrave(grave);
        }
    }
}
