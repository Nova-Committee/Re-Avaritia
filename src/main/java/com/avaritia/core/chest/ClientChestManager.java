//package com.avaritia.core.chest;
//
//import net.minecraft.nbt.CompoundTag;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
//
//import java.util.UUID;
//
///**
// * @author cnlimiter
// */
////@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
//public class ClientChestManager {
//    private static volatile ClientChestManager instance;
//
//    public static ClientChestManager getInstance() {
//        if (instance == null) {
//            synchronized (ClientChestManager.class) {
//                if (instance == null) instance = new ClientChestManager();
//            }
//        }
//        return instance;
//    }
//
//    @SubscribeEvent
//    public static void onLoggingInServer(ClientPlayerNetworkEvent.LoggingIn event) {
//        if (instance == null) {
//            synchronized (ClientChestManager.class) {
//                if (instance == null) instance = new ClientChestManager();
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public static void onLoggingOutServer(ClientPlayerNetworkEvent.LoggingOut event) {
//        instance = null;
//    }
//
//
//    private CompoundTag userCache;
//
//
//    public ClientChestManager() {}
//
//    public void setUserCache(CompoundTag userCache) {
//        this.userCache = userCache;
//    }
//
//    public CompoundTag getUserCache() {
//        return userCache;
//    }
//
//    public String getUserName(UUID uuid) {
//        String userName = userCache.getCompound("nameCache").getString(uuid.toString());
//        if (userName.isEmpty()) return "unknownUser";
//        return userName;
//    }
//
//}
