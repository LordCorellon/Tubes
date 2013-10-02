package schmoller.tubes.render;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import schmoller.tubes.ITube;
import schmoller.tubes.definitions.InjectionTube;
import schmoller.tubes.definitions.TubeDefinition;

public class InjectionTubeRender extends NormalTubeRender
{
	@Override
	public boolean renderDynamic( TubeDefinition type, ITube tube, World world, int x, int y, int z )
	{
		return false;
	}
	
	@Override
	public void renderStatic( TubeDefinition type, ITube tube, World world, int x, int y, int z )
	{
		super.renderStatic(type, tube, world, x, y, z);
		
		mRender.setIcon(InjectionTube.coreIcon);
		mRender.drawBox(63, 0.1875f, 0.1875f, 0.1875f, 0.8125f, 0.8125f, 0.8125f);
	}
	
	@Override
	public void renderItem( TubeDefinition type, ItemStack item )
	{
		mRender.resetTransform();
		mRender.enableNormals = true;
		mRender.resetTextureFlip();
		mRender.resetTextureRotation();
		mRender.resetLighting(15728880);
		
		mRender.setLocalLights(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		
		Tessellator tes = Tessellator.instance;
		
		FMLClientHandler.instance().getClient().renderGlobal.renderEngine.bindTexture("/terrain.png");
		tes.startDrawingQuads();
		
		mRender.setIcon(InjectionTube.coreIcon);
		mRender.drawBox(63, 0.1875f, 0.1875f, 0.1875f, 0.8125f, 0.8125f, 0.8125f);
		
		tes.draw();
	}
}