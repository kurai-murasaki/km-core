package dev.Ox6b6d.kmCore.module.impl.mechanics.safe;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class SafeData {

    private final UUID safeId;
    private UUID ownerUuid;
    private ItemStack[] contents; // 54 slots (double chest)

    public SafeData(UUID safeId, UUID ownerUuid) {
        this.safeId = safeId;
        this.ownerUuid = ownerUuid;
        this.contents = new ItemStack[54];
    }

    public UUID getSafeId()    { return safeId; }
    public UUID getOwnerUuid() { return ownerUuid; }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = new ItemStack[54];
        for (int i = 0; i < Math.min(contents.length, 54); i++) {
            this.contents[i] = contents[i] != null ? contents[i].clone() : null;
        }
    }
}