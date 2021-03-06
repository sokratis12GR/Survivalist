package gigaherz.survivalist.scraping;

import com.google.common.collect.Lists;
import gigaherz.survivalist.ConfigManager;
import gigaherz.survivalist.Survivalist;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ItemBreakingTracker
{
    public static final ResourceLocation PROP_KEY = new ResourceLocation(Survivalist.MODID, "ItemBreakingTracker");

    EntityPlayer player;
    World world;

    ItemStack[] equipmentSlots;

    public static ItemBreakingTracker get(EntityPlayer p)
    {
        return p.getCapability(Handler.TRACKER, null);
    }

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    public void init(Entity entity, World world)
    {
        this.player = (EntityPlayer) entity;
        this.world = world;
    }

    public void before()
    {
        List<ItemStack> equipment = Lists.newArrayList(player.getArmorInventoryList());
        equipmentSlots = new ItemStack[equipment.size()];
        for (int i = 0; i < equipment.size(); i++)
        {
            ItemStack stack = equipment.get(i);
            equipmentSlots[i] = stack != null ? stack.copy() : null;
        }
    }

    public Collection<ItemStack> after()
    {
        List<ItemStack> changes = Lists.newArrayList();
        List<ItemStack> equipment = Lists.newArrayList(player.getArmorInventoryList());
        for (int i = 0; i < equipment.size(); i++)
        {
            ItemStack stack2 = equipmentSlots[i];
            if (stack2 != null)
            {
                ItemStack stack = equipment.get(i);
                if (stack == null)
                {
                    changes.add(stack2);
                }
            }
        }
        return changes;
    }

    public static class Handler
    {
        final Random rnd = new Random();

        @CapabilityInject(ItemBreakingTracker.class)
        public static Capability<ItemBreakingTracker> TRACKER;

        public static Handler instance;

        List<Triple<ItemStack, ItemStack, ItemStack>> scrapingRegistry = Lists.newArrayList();

        public Handler()
        {
            instance = this;

            CapabilityManager.INSTANCE.register(ItemBreakingTracker.class, new Capability.IStorage<ItemBreakingTracker>()
            {
                @Override
                public NBTBase writeNBT(Capability<ItemBreakingTracker> capability, ItemBreakingTracker instance, EnumFacing side)
                {
                    return null;
                }

                @Override
                public void readNBT(Capability<ItemBreakingTracker> capability, ItemBreakingTracker instance, EnumFacing side, NBTBase nbt)
                {

                }
            }, () -> null);

            registerScrapoingConversions();
        }

        void registerScrapoingConversions()
        {
            if (ConfigManager.instance.enableToolScraping)
            {
                scrapingRegistry.add(Triple.of(new ItemStack(Items.wooden_shovel), new ItemStack(Blocks.planks), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.wooden_hoe), new ItemStack(Blocks.planks), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.wooden_axe), new ItemStack(Blocks.planks), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.wooden_pickaxe), new ItemStack(Blocks.planks), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.wooden_sword), new ItemStack(Blocks.planks), new ItemStack(Items.stick)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.stone_shovel), new ItemStack(Blocks.cobblestone), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.stone_hoe), new ItemStack(Blocks.cobblestone), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.stone_axe), new ItemStack(Blocks.cobblestone), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.stone_pickaxe), new ItemStack(Blocks.cobblestone), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.stone_sword), new ItemStack(Blocks.cobblestone), new ItemStack(Items.stick)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_shovel), new ItemStack(Items.iron_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_hoe), new ItemStack(Items.iron_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_axe), new ItemStack(Items.iron_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_pickaxe), new ItemStack(Items.iron_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_sword), new ItemStack(Items.iron_ingot), new ItemStack(Items.stick)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_shovel), new ItemStack(Items.gold_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_hoe), new ItemStack(Items.gold_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_axe), new ItemStack(Items.gold_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_pickaxe), new ItemStack(Items.gold_ingot), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_sword), new ItemStack(Items.gold_ingot), new ItemStack(Items.stick)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_shovel), new ItemStack(Items.diamond), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_hoe), new ItemStack(Items.diamond), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_axe), new ItemStack(Items.diamond), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_pickaxe), new ItemStack(Items.diamond), new ItemStack(Items.stick)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_sword), new ItemStack(Items.diamond), new ItemStack(Items.stick)));
            }

            if (ConfigManager.instance.enableArmorScraping)
            {
                scrapingRegistry.add(Triple.of(new ItemStack(Items.leather_boots), new ItemStack(Items.leather, 2), new ItemStack(Items.leather)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.leather_helmet), new ItemStack(Items.leather, 2), new ItemStack(Items.leather)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.leather_chestplate), new ItemStack(Items.leather, 2), new ItemStack(Items.leather)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.leather_leggings), new ItemStack(Items.leather, 2), new ItemStack(Items.leather)));

                scrapingRegistry.add(Triple.of(new ItemStack(Survivalist.tanned_boots), new ItemStack(Survivalist.tanned_leather, 2), new ItemStack(Survivalist.tanned_leather)));
                scrapingRegistry.add(Triple.of(new ItemStack(Survivalist.tanned_helmet), new ItemStack(Survivalist.tanned_leather, 2), new ItemStack(Survivalist.tanned_leather)));
                scrapingRegistry.add(Triple.of(new ItemStack(Survivalist.tanned_chestplate), new ItemStack(Survivalist.tanned_leather, 2), new ItemStack(Survivalist.tanned_leather)));
                scrapingRegistry.add(Triple.of(new ItemStack(Survivalist.tanned_leggings), new ItemStack(Survivalist.tanned_leather, 2), new ItemStack(Survivalist.tanned_leather)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.chainmail_boots), new ItemStack(Survivalist.chainmail, 2), new ItemStack(Survivalist.chainmail)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.chainmail_helmet), new ItemStack(Survivalist.chainmail, 2), new ItemStack(Survivalist.chainmail)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.chainmail_chestplate), new ItemStack(Survivalist.chainmail, 2), new ItemStack(Survivalist.chainmail)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.chainmail_leggings), new ItemStack(Survivalist.chainmail, 2), new ItemStack(Survivalist.chainmail)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_boots), new ItemStack(Items.iron_ingot, 2), new ItemStack(Items.iron_ingot)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_helmet), new ItemStack(Items.iron_ingot, 2), new ItemStack(Items.iron_ingot)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_chestplate), new ItemStack(Items.iron_ingot, 2), new ItemStack(Items.iron_ingot)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.iron_leggings), new ItemStack(Items.iron_ingot, 2), new ItemStack(Items.iron_ingot)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_boots), new ItemStack(Items.gold_ingot, 2), new ItemStack(Items.gold_ingot)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_helmet), new ItemStack(Items.gold_ingot, 2), new ItemStack(Items.gold_ingot)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_chestplate), new ItemStack(Items.gold_ingot, 2), new ItemStack(Items.gold_ingot)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.golden_leggings), new ItemStack(Items.gold_ingot, 2), new ItemStack(Items.gold_ingot)));

                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_boots), new ItemStack(Items.diamond, 2), new ItemStack(Items.diamond)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_helmet), new ItemStack(Items.diamond, 2), new ItemStack(Items.diamond)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_chestplate), new ItemStack(Items.diamond, 2), new ItemStack(Items.diamond)));
                scrapingRegistry.add(Triple.of(new ItemStack(Items.diamond_leggings), new ItemStack(Items.diamond, 2), new ItemStack(Items.diamond)));
            }
        }

        private void onItemBroken(EntityPlayer player, ItemStack stack)
        {
            int survivalism = EnchantmentHelper.getEnchantmentLevel(Survivalist.scraping, stack);
            boolean fortune = rnd.nextDouble() > 0.9 / (1 + survivalism);

            ItemStack ret = null;

            for (Triple<ItemStack, ItemStack, ItemStack> scraping : scrapingRegistry)
            {
                ItemStack source = scraping.getLeft();

                if (source.getItem() != stack.getItem())
                    continue;

                ItemStack good = scraping.getMiddle();
                ItemStack bad = scraping.getRight();

                ret = fortune ? good.copy() : bad.copy();

                break;
            }

            if (ret != null)
            {
                Survivalist.logger.warn("Item broke (" + stack + ") and the player got " + ret + " in return!");

                player.addChatMessage(new TextComponentString("Item broke (" + stack + ") and the player got " + ret + " in return!"));

                EntityItem entityitem = new EntityItem(player.worldObj, player.posX, player.posY + 0.5, player.posZ, ret);
                entityitem.motionX = 0;
                entityitem.motionZ = 0;

                player.worldObj.spawnEntityInWorld(entityitem);
            }
        }

        @SubscribeEvent
        public void onPlayerDestroyItem(PlayerDestroyItemEvent ev)
        {
            if (ev.getEntityPlayer().worldObj.isRemote)
                return;

            ItemStack stack = ev.getOriginal();

            Item item = stack.getItem();
            if (!(item instanceof ItemTool))
                return;

            onItemBroken(ev.getEntityPlayer(), stack);
        }

        @SubscribeEvent
        public void onLivingHurt(LivingHurtEvent ev)
        {
            if (ev.getEntity().worldObj.isRemote)
                return;

            if (ev.getEntity() instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) ev.getEntityLiving();

                ItemBreakingTracker.get(player).before();
            }
        }

        @SubscribeEvent
        public void entityJoinWorld(EntityJoinWorldEvent ev)
        {
            if (ev.getEntity().worldObj.isRemote)
                return;

            if (ev.getEntity() instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) ev.getEntity();

                CombatTrackerIntercept interceptTracker = new CombatTrackerIntercept(player);
                ReflectionHelper.setPrivateValue(EntityLivingBase.class, player, interceptTracker,
                        "field_94063_bt", "_combatTracker");
            }
        }

        public void onTrackDamage(EntityPlayer player)
        {
            Collection<ItemStack> missing = ItemBreakingTracker.get(player).after();
            for (ItemStack broken : missing)
            {
                onItemBroken(player, broken);
            }
        }

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent.Entity e)
        {
            final Entity entity = e.getEntity();

            if (entity.worldObj.isRemote)
                return;

            if (entity instanceof EntityPlayer)
            {
                if (!entity.hasCapability(TRACKER, null))
                {
                    e.addCapability(PROP_KEY, new ICapabilityProvider()
                    {
                        ItemBreakingTracker cap = new ItemBreakingTracker();

                        {
                            cap.init(entity, entity.worldObj);
                        }

                        @Override
                        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                        {
                            return capability == TRACKER;
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                        {
                            if (capability == TRACKER)
                                return (T) cap;
                            return null;
                        }
                    });
                }
            }
        }
    }

    // Forwards all calls to the existing instance, so that if some other mod overrides this class, it will still work as expected
    public static class CombatTrackerIntercept extends CombatTracker
    {
        CombatTracker inner;
        EntityPlayer entity;

        public CombatTrackerIntercept(EntityPlayer fighterIn)
        {
            super(fighterIn);
            inner = fighterIn.getCombatTracker();
            entity = fighterIn;
        }

        @Override
        public void trackDamage(DamageSource damageSrc, float healthIn, float damageAmount)
        {
            Handler.instance.onTrackDamage(entity);

            inner.trackDamage(damageSrc, healthIn, damageAmount);
        }

        @Override
        public int func_180134_f()
        {
            return inner.func_180134_f();
        }

        @Override
        public EntityLivingBase getBestAttacker()
        {
            return inner.getBestAttacker();
        }

        @Override
        public void calculateFallSuffix()
        {
            inner.calculateFallSuffix();
        }

        @Override
        public ITextComponent getDeathMessage()
        {
            return inner.getDeathMessage();
        }

        @Override
        public EntityLivingBase getFighter()
        {
            return inner.getFighter();
        }

        @Override
        public void reset()
        {
            inner.reset();
        }
    }
}
