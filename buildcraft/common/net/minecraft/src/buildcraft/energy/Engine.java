package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.buildcraft.api.Orientations;

public abstract class Engine {

	public int maxEnergy;
	public float progress;
	public Orientations orientation;
	int energy;	

	protected TileEngine tile;
	
	enum EnergyStage {
		Blue,
		Green,
		Yellow,
		Red,
		Explosion
	}
	
	public Engine (TileEngine tile) {
		this.tile = tile;
	}
			
	public EnergyStage getEnergyStage () {
		if (energy / (double) maxEnergy * 100.0 <= 25.0) {
			return EnergyStage.Blue;
		} else if (energy / (double) maxEnergy * 100.0 <= 50.0) {
		 	return EnergyStage.Green;
		}  else if (energy / (double) maxEnergy * 100.0 <= 75.0) {
			return EnergyStage.Yellow;
		} else if (energy / (double) maxEnergy * 100.0 <= 100.0) {
			return EnergyStage.Red;
		} else {
			return EnergyStage.Explosion;
		}
	}	
	
	public void update () {
		
	}
	
	public abstract String getTextureFile ();
	
	public abstract int explosionRange ();
	
	public abstract int maxEnergyReceived ();
	
	public abstract float getPistonSpeed ();
	
	public abstract boolean isBurning ();

	public void addEnergy (int addition) {
		energy += addition;
		
		if (getEnergyStage() == EnergyStage.Explosion) {
			tile.worldObj.createExplosion(null, tile.xCoord, tile.yCoord,
					tile.zCoord, explosionRange());
		}
	}
}
