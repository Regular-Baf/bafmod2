package baf.quest

import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

class NitwitVillagerScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    private val villager: VillagerEntity
) : ScreenHandler(
    ScreenHandlerType.GENERIC_9X2,
    syncId
) {
    private val inventory: Inventory = object : Inventory {
        override fun clear() {
            // Implement the logic to clear the inventory here
        }

        override fun size(): Int {
            return 2
        }

        override fun isEmpty(): Boolean {
            return false
        }

        override fun getStack(slot: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun removeStack(slot: Int, amount: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun removeStack(slot: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun setStack(slot: Int, stack: ItemStack) {

        }

        override fun markDirty() {

        }

        override fun canPlayerUse(player: PlayerEntity): Boolean {
            return true
        }
    }

    init {
        // Villager's inventory slots
        addSlot(Slot(inventory, 0, 80, 18))
        addSlot(Slot(inventory, 1, 80, 54))

        // Player's inventory slots
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                val x = 8 + col * 18
                val y = 84 + row * 18
                addSlot(Slot(playerInventory, col + row * 9 + 9, x, y))
            }
        }

        // Player's hotbar slots
        for (col in 0 until 9) {
            val x = 8 + col * 18
            val y = 142
            addSlot(Slot(playerInventory, col, x, y))
        }
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]

        if (slot.hasStack()) {
            val stackInSlot = slot.stack
            itemStack = stackInSlot.copy()

            if (index < 2) {
                if (!insertItem(stackInSlot, 2, 38, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY
            }

            if (stackInSlot.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }

        return itemStack
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
        // Handle slot clicks and actions here

        // Example: Prevent interacting with slots in the screen
        if (slotIndex < 2) {
            return
        }

        super.onSlotClick(slotIndex, button, actionType, player)
    }

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        // Implement the logic for quick move here
        return ItemStack.EMPTY
    }
}
