package edu.umich.soar.gridmap2d.visuals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import edu.umich.soar.gridmap2d.Gridmap2D;
import edu.umich.soar.gridmap2d.config.PlayerConfig;
import edu.umich.soar.gridmap2d.map.GridMap;
import edu.umich.soar.gridmap2d.players.Player;


public class AgentDisplay extends Composite {
	
	public AgentDisplay(final Composite parent) {
		super(parent, SWT.NONE);
	}

	void selectPlayer(Player player) {
		assert false;
	}

	void worldChangeEvent() {
		assert false;
	}

	void updateButtons() {
		assert false;
	}

	void agentEvent() {
		assert false;
	}

	public void setMap(GridMap map) {
	}
	
	private static int clonePlayer = 0;
	void clonePlayer(String playerId) {
		PlayerConfig existingPlayerConfig = Gridmap2D.config.playerConfigs().get(playerId);
		
		// create id and configuration
		String clonePlayerId = "clone" + Integer.toString(++clonePlayer);
		PlayerConfig clonePlayerConfig = new PlayerConfig();

		if (existingPlayerConfig.productions != null) {
			clonePlayerConfig.productions = new String(existingPlayerConfig.productions);
		}
		
		if (existingPlayerConfig.script != null) {
			clonePlayerConfig.script = new String(existingPlayerConfig.script);
		}
		
		Gridmap2D.config.playerConfigs().put(clonePlayerId, clonePlayerConfig);
		Gridmap2D.simulation.createPlayer(clonePlayerId, clonePlayerConfig, false);
	}
}
