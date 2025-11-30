package com.dib.services;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.dib.repository.SantaRepository;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class SantaNPCManager {
    private final JavaPlugin plugin;
    private final SantaRepository santaRepository;
    private final Logger logger;
    private UUID santaUUID;

    public SantaNPCManager(JavaPlugin plugin, SantaRepository santaRepository) {
        this.plugin = plugin;
        this.santaRepository = santaRepository;
        this.logger = plugin.getLogger();
    }

    public boolean spawnSanta(Location location) {
        if (getSantaEntity().isPresent()) {
            return false;
        }

        Mannequin santa = (Mannequin) location.getWorld().spawnEntity(location, EntityType.MANNEQUIN);

        // Set the Santa profile/skin
        PlayerProfile santaProfile = Bukkit.createProfile("Santa");
        santa.setProfile(ResolvableProfile.resolvableProfile(santaProfile));
        // Set custom name (without NPC tag by not making it visible)
        santa.customName(Component.text("Santa")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD));
        santa.setCollidable(false);
        santa.setInvulnerable(true);
        santa.setGravity(false);
        santa.setPersistent(true);

        this.santaUUID = santa.getUniqueId();

        santaRepository.saveSantaLocation(location);

        logger.info("Santa NPC spawned at " + location);
        return true;
    }

    public boolean removeSanta() {
        Optional<Entity> santaOpt = getSantaEntity();

        if (santaOpt.isEmpty()) {
            return false;
        }

        santaOpt.get().remove();
        this.santaUUID = null;
        santaRepository.deleteSantaLocation();

        logger.info("Santa NPC removed");
        return true;
    }

    public void loadSantaFromDatabase() {
        Optional<Location> locationOpt = santaRepository.getSantaLocation();

        if (locationOpt.isEmpty()) {
            return;
        }

        Location location = locationOpt.get();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (location.getWorld() == null) {
                logger.warning("World not loaded for Santa NPC. Skipping spawn.");
                return;
            }

            boolean found = false;
            for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
                if (entity instanceof Mannequin mannequin &&
                        mannequin.customName() != null &&
                        Component.text("Santa").equals(mannequin.customName())) {
                    this.santaUUID = mannequin.getUniqueId();
                    found = true;
                    logger.info("Found existing Santa NPC");
                    break;
                }
            }

            if (!found) {
                spawnSanta(location);
                logger.info("Respawned Santa NPC from database");
            }
        }, 40L);
    }

    public Optional<Entity> getSantaEntity() {
        if (santaUUID == null) {
            return Optional.empty();
        }

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(santaUUID);
            if (entity != null && entity.isValid()) {
                return Optional.of(entity);
            }
        }

        this.santaUUID = null;
        return Optional.empty();
    }

    public boolean isSanta(int entityId) {
        Optional<Entity> santaOpt = getSantaEntity();
        return santaOpt.isPresent() && santaOpt.get().getEntityId() == entityId;
    }

    public Location getSantaLocation() {
        return getSantaEntity().map(Entity::getLocation).orElse(null);
    }
}
