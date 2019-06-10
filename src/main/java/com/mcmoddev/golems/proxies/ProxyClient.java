package com.mcmoddev.golems.proxies;

import com.mcmoddev.golems.entity.base.GolemBase;
import com.mcmoddev.golems.entity.base.GolemColorized;
import com.mcmoddev.golems.main.ExtraGolems;
import com.mcmoddev.golems.renders.RenderColoredGolem;
import com.mcmoddev.golems.renders.RenderGolem;
import com.mcmoddev.golems.util.BlockTagUtil;
import com.mcmoddev.golems.util.config.GolemRegistrar;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public final class ProxyClient extends ProxyCommon {

	public static final IRenderFactory<GolemBase> FACTORY_TEXTURED_GOLEM = RenderGolem::new;

	public static final IRenderFactory<GolemColorized> FACTORY_COLORED_GOLEM = RenderColoredGolem::new;
	
	@Override
	public void registerListeners() {
		final IResourceManager irr = Minecraft.getInstance().getResourceManager();
		if(irr instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) irr).addReloadListener(l -> BlockTagUtil.loadTags());
		}
	}
	
	@Override
	public void registerEntityRenders() {
		GolemRegistrar.getContainers().forEach(container -> registerEntityRender(container.entityType.getEntityClass()));
	}

	/** 
	 * Helper function for entity rendering registration.
	 * If the class inherits from {@code GolemColorized.class}, 
	 * then it will be register using  {@link #registerColorized}.
	 * Otherwise, the class will be registered using
	 * {@link #registerTextured(Class)} by default.
	 */
	public static void registerEntityRender(final Class<? extends GolemBase> clazz) {
		if(GolemColorized.class.isAssignableFrom(clazz)) {
			registerColorized((Class<? extends GolemColorized>)clazz);
		} else {
			registerTextured(clazz);
		}
	}

	/**
	 * Registers an entity with the RenderGolem rendering class.
	 */
	public static void registerTextured(final Class<? extends GolemBase> golem) {
		RenderingRegistry.registerEntityRenderingHandler(golem, FACTORY_TEXTURED_GOLEM);
	}

	public static void registerColorized(final Class<? extends GolemColorized> golem) {
		RenderingRegistry.registerEntityRenderingHandler(golem, FACTORY_COLORED_GOLEM);
	}
}