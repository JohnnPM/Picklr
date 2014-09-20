package com.twemyeez.picklr.friends;

import java.util.ArrayList;
import java.util.List;


import java.util.Timer;
import java.util.TimerTask;

import com.twemyeez.picklr.listener.ChatListener;
import com.twemyeez.picklr.listener.ChatListener.ChatStatus;
import com.twemyeez.picklr.utils.CommonUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class OnlineListManager {
	
	//This holds the friends, and their location
	public static List<Friend> friends = new ArrayList<Friend>();
	
	//This buffers messages so they don't interrupt the list
	public static List<IChatComponent> messageBuffer = new ArrayList<IChatComponent>();
	
	public static Boolean isInProgress()
	{
		//There are multiple different status's associated with the friend list manager. As a result, this method simplifies it, by checking for any.
		return (ChatListener.currentStatus.contains(ChatStatus.FRIEND_GETTING_PAGE) || ChatListener.currentStatus.contains(ChatStatus.FRIEND_LISTING));
	}
	
	/*
	 * This method is used to initiate the start of a friend list, if one isn't already in progress
	 */
	public static void runFriendList()
	{
		//If it's not already in progress
		if(!isInProgress())
		{
			//Add the chat status so we now listen for the page
			ChatListener.currentStatus.add(ChatStatus.FRIEND_GETTING_PAGE);
			//Send the initial friend list request
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/f list");
			//Tell the user that it's being listed
			CommonUtils.sendFormattedChat(true, "Friends online:", EnumChatFormatting.RED, false);
		}		
	}
	
	public static void relatedChatEventHandler(ClientChatReceivedEvent event)
	{
		//We know that the message isn't null
		String message = event.message.getUnformattedText();
		
		//Now let's check if we're getting the page
		if(ChatListener.currentStatus.contains(ChatListener.ChatStatus.FRIEND_GETTING_PAGE))
		{
			//Let's check the message starts with the dashes we'd expect
			if(message.startsWith("--- "))
			{
				//Split the message into words
				String[] messageSplit = message.split(" ");
				//Cancel them getting the message
				event.setCanceled(true);
				
				//Now let's try to parse the number of pages and save it for future use
				try
				{
					//Save the response pages
					int pagesOfResponse = Integer.valueOf(messageSplit[5].replace(")", ""));
					
					//Remove this status, we've now got the correct number of pages
					ChatListener.currentStatus.remove(ChatStatus.FRIEND_GETTING_PAGE);
					//Now set the status to listen for the actual friend list
					ChatListener.currentStatus.add(ChatStatus.FRIEND_LISTING);
					
					//Send the request for the extra pages if there are any
					if (pagesOfResponse > 1)
			    	{
			    		//then for second page onwards, request it's sent
			    		for(int i=2; i <= pagesOfResponse; i++)
						{
			    			Minecraft.getMinecraft().thePlayer.sendChatMessage("/f list "  + String.valueOf(i));
						}
			    	}
					
					//Clear the previous messageBuffer
					messageBuffer.clear();
					
					//Now, we run a timer in a short while to deliver messages stopped while friends were listing
					Timer timer = new Timer();
					
					//Schedule the messages to be sent in a certain amount of time
					timer.schedule(new TimerTask(){

						@Override
						public void run() {
							for(IChatComponent message: messageBuffer)
					    	{
					    		//Iterate through the messages and resend them
					    		Minecraft.getMinecraft().thePlayer.addChatMessage(message);
					    	}
						}
						
					}, pagesOfResponse*200);
					
				}
				catch(Exception e)
				{
					//Oh dear, we failed to parse the integer!
					e.printStackTrace();
				}
				
			}
		}
		//Otherwise, we're getting the actual friend list
		else if(ChatListener.currentStatus.contains(ChatListener.ChatStatus.FRIEND_LISTING))
		{
			/*
			 * There's no easy way we can check for a friend list, so what we'll do is check it's not got any join
			 * or punch or : messages in it. This indicates it's not chat, a join message, or a punch.
			 * 
			 * This probably leaves some things that don't get filtered
			 */
			if(message.indexOf(":") == -1 && message.indexOf("punched") == -1 && message.indexOf("joined") == -1)
			{
				//We'll split into words for further processing
				String[] messageSplit = message.split(" ");
				
				//Check for "is" to ensure it's a status
				if(messageSplit[1].equals("is"))
				{
					//This confirms it's from the friend list. Let's check they're not offline
					if(messageSplit[3].equals("offline") != true)
					{
						//
					}
					else
					{
						//If the fourth word is "offline" it means they're offline, so we hide them
						event.setCanceled(true);
					}
				} 
				else
				{
					//This means it's probably not a status message, so we'll save it and show it later
					saveMessageToBuffer(event.message);
					//Cancel the message
					event.setCanceled(true);
				}
			}
			else
			{
				//This means it's probably not a status message, so we'll save it and show it later
				saveMessageToBuffer(event.message);
				//Cancel the message
				event.setCanceled(true);
			}
		}
	}
	
	private static void saveMessageToBuffer(IChatComponent message) {
		messageBuffer.add(message);
	}

}