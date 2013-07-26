package com.azwave.androidzwave.zwave.nodes;

import java.util.EventListener;

public interface NodeListener extends EventListener {

	public void onNodeAliveListener(boolean alive);

	public void onNodeQueryStageCompleteListener();

	public void onNodeAddedToList();

	public void onNodeRemovedToList();

}
