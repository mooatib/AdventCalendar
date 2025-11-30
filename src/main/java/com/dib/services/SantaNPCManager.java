package com.dib.services;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class SantaNPCManager {
    private final Logger logger;
    private UUID santaUUID;

    public SantaNPCManager(Logger logger) {
        this.logger = logger;
    }

    private void applySantaProperties(Mannequin santa) {
        PlayerProfile santaProfile = Bukkit.createProfile("Santa");
        santa.setProfile(ResolvableProfile.resolvableProfile(santaProfile));
        santa.customName(Component.text("Santa")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD));
        santa.setCollidable(false);
        santa.setInvulnerable(true);
        santa.setGravity(false);
        santa.setPersistent(true);
    }

    public void reapplySantaProperties(Mannequin santa) {
        applySantaProperties(santa);
        logger.info("Reapplied Santa NPC properties after entity load");
    }

    public boolean tryCaptureSantaEntity(Mannequin mannequin) {
        if (mannequin.customName() == null) {
            return false;
        }

        String name = PlainTextComponentSerializer.plainText().serialize(mannequin.customName());
        if ("Santa".equals(name)) {
            if (this.santaUUID == null || !this.santaUUID.equals(mannequin.getUniqueId())) {
                this.santaUUID = mannequin.getUniqueId();
                applySantaProperties(mannequin);
                logger.info("Captured existing Santa NPC with UUID: " + this.santaUUID);
                return true;
            }
        }
        return false;
    }

    public boolean spawnSanta(Location location) {
        if (getSantaEntity().isPresent()) {
            return false;
        }
        Mannequin santa = (Mannequin) location.getWorld().spawnEntity(location, EntityType.MANNEQUIN);
        applySantaProperties(santa);
        this.santaUUID = santa.getUniqueId();
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
        logger.info("Santa NPC removed");
        return true;
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
