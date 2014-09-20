package com.twemyeez.picklr.utils;

import org.lwjgl.input.Keyboard;

import com.twemyeez.picklr.friends.OnlineListManager;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

public class KeyBindings {
	public static KeyBinding friendList = new KeyBinding("Friends list", Keyboard.KEY_F, "PickledChat");
	
	public KeyBindings()
	{
		//Register the friend list key binding
		ClientRegistry.registerKeyBinding(friendList);
	}
	
	@SubscribeEvent
	public void KeyInputEvent(KeyInputEvent event) {
		/*
		 * There's been a key input event, so let's check what's been pressed
		 */
		
		//For the online friend list
		if(friendList.isPressed())
		{
			OnlineListManager.runFriendList();
		}
	}
}
