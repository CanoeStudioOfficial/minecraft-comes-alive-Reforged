package mca.items;

import mca.core.MCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ItemLetter extends Item {

    public ItemLetter() {
        setTranslationKey("letter");
        setRegistryName(MCA.MODID, "letter");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // 在客户端打开书籍GUI
        if (world.isRemote) {
            openBookGUI(stack);
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    private void openBookGUI(ItemStack stack) {
        Minecraft.getMinecraft().displayGuiScreen(
            new GuiScreenBook(
                Minecraft.getMinecraft().player,
                stack,
                false
            )
        );
    }

    /**
     * 创建一封信件
     * @param itemLetter 信件物品
     * @param title 信件标题
     * @param pages 信件内容页面列表
     * @return 信件物品堆
     */
    public static ItemStack createLetter(Item itemLetter, String title, List<String> pages) {
        ItemStack stack = new ItemStack(itemLetter);
        NBTTagCompound nbt = new NBTTagCompound();

        // 设置作者
        nbt.setString("author", "MCA Villager");

        // 设置标题
        nbt.setString("title", title);

        // 设置页面内容
        NBTTagList pagesList = new NBTTagList();
        for (String page : pages) {
            pagesList.appendTag(new NBTTagString(page));
        }
        nbt.setTag("pages", pagesList);

        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * 从ITextComponent列表创建信件
     * @param itemLetter 信件物品
     * @param title 信件标题
     * @param pages 信件内容页面列表
     * @return 信件物品堆
     */
    public static ItemStack createLetterFromComponents(Item itemLetter, String title, List<ITextComponent> pages) {
        ItemStack stack = new ItemStack(itemLetter);
        NBTTagCompound nbt = new NBTTagCompound();

        // 设置作者
        nbt.setString("author", "MCA Villager");

        // 设置标题
        nbt.setString("title", title);

        // 设置页面内容
        NBTTagList pagesList = new NBTTagList();
        for (ITextComponent page : pages) {
            pagesList.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(page)));
        }
        nbt.setTag("pages", pagesList);

        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * 获取信件的页面内容
     * @param stack 信件物品堆
     * @return 页面内容列表
     */
    public static List<String> getPages(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return null;
        }

        NBTTagList pagesList = stack.getTagCompound().getTagList("pages", 8);
        List<String> pages = new ArrayList<>();

        for (int i = 0; i < pagesList.tagCount(); i++) {
            pages.add(pagesList.getStringTagAt(i));
        }

        return pages;
    }

    /**
     * 检查物品堆是否是信件
     * @param stack 物品堆
     * @return 是否是信件
     */
    public static boolean isLetter(ItemStack stack) {
        return stack.getItem() instanceof ItemLetter;
    }
}
