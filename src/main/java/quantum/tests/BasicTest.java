//
// Copyright (c) 2009 Mario Zechner.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the GNU Lesser Public License v2.1
// which accompanies this distribution, and is available at
// http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
// 
// Contributors:
//     Mario Zechner - initial API and implementation
//
package quantum.tests;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.swing.JFrame;

import quantum.game.Bot;
import quantum.game.GameLoop;
import quantum.game.GameRecorder;
import quantum.game.Simulation;
import quantum.game.commands.AllyCommand;
import quantum.net.Client;
import quantum.net.Server;
import quantum.net.messages.DisconnectedMessage;
import quantum.net.messages.MapListMessage;
import quantum.net.messages.Message;
import quantum.net.messages.PlayerListMessage;
import quantum.net.messages.ReadyMessage;
import quantum.net.messages.SimulationMessage;

import com.sun.opengl.util.Animator;


public class BasicTest extends JFrame implements GLEventListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3199225477234904782L;
	Client client;	
	Simulation sim;
	GameLoop game;
	Server server = new Server( 7776, "Test Server", true, "" );
	Bot bot;
	Bot bot2;
	Bot bot3;
	Bot bot4;
	GameRecorder saver;
	
	public BasicTest( ) throws Exception
	{		
//		server.setLogging( true );
		client = new Client( "marzec", "localhost", 7776 );				
//		bot = new Bot( "dat/scripts/simplebot.bsh", "bot", client );
		bot2 = new Bot( "dat/scripts/simplebot.bsh", "bot 1", client );
		bot3 = new Bot( "dat/scripts/simplebot.bsh", "bot 2", client );
//		bot4 = new Bot( "dat/scripts/simplebot.bsh", "bot 3", client );
		client.sendMessage( new ReadyMessage( client.getPlayer().getId(), client.getPlayer().getName() ) );		
		
		SimulationMessage sim_msg = null;
		PlayerListMessage player_msg = null;
		
		while( true )
		{
			Message msg = client.readMessage();
			
			
			if( msg instanceof PlayerListMessage )
			{
				client.setPlayerList( (PlayerListMessage)msg );
				player_msg = client.getPlayerList();
			}
			
			if( msg instanceof SimulationMessage )
			{
				sim = ((SimulationMessage)msg).getSimulation();
				sim.setClient(client);
				sim.setAlly( client.getPlayer().getId(), bot2.getId(), AllyCommand.ALLY);
				sim.setAlly( bot2.getId(), client.getPlayers().get(0).getId(), AllyCommand.ALLY);
				sim.moveCreatures( client.getPlayer().getId(), 0, 2, 5);
				sim.moveCreatures( bot2.getId(), 1, 2, 1);
//				sim.setAlly( client.get, 10000, AllyCommand.ALLY);
//				sim.setAlly( 10000, client.getPlayer().getId(), AllyCommand.ALLY);
				game = new GameLoop( client, sim, false );	
				sim_msg = (SimulationMessage)msg;
				break;
			}
			
			if( msg instanceof MapListMessage )
			{
				for( String map: ((MapListMessage)msg).getMaps() )
					System.out.println( map );
			}
			
			Thread.sleep( 10 );
		}		    
		
		saver = new GameRecorder( sim_msg, player_msg );
		
		GLCapabilities caps = new GLCapabilities();
		caps.setRedBits(8);
    	caps.setGreenBits(8);
    	caps.setBlueBits(8);
    	caps.setAlphaBits(8);
    	caps.setDepthBits(16);
    	caps.setStencilBits(8);
    	caps.setDoubleBuffered(true);
        GLCanvas canvas = new GLCanvas( caps );      
        canvas.addGLEventListener(this);

        setSize(1024,1024);
        setTitle("CAV-Projekt: JOGL - Beispielszene");        

        getContentPane().add(canvas,BorderLayout.CENTER);

        final Animator animator = new Animator( canvas );
        animator.setRunAsFastAsPossible( true );
        animator.start();
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	animator.stop();
            	try {
					saver.dispose( "game.sav" );
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	closing( );
                System.exit(0);
            }
        });
	}	
	
	public void closing() 
	{						
		if( client != null )
		{
			DisconnectedMessage msg = new DisconnectedMessage( client.getPlayer().getName() );
			try {
				client.sendMessage(msg);
			} catch (Exception e) {
			}
			client.dispose();
		}
	}
	
	public static void main( String[] argv ) throws Exception
	{			
		new BasicTest( );
	}

	public void display(GLAutoDrawable canvas) {
		GL gl = canvas.getGL();
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
			
//		game.setLogging( true );
		game.update((GLCanvas)canvas);				
		game.render((GLCanvas)canvas);
//		bot.update( sim );
		bot2.update( sim );		
		bot3.update( sim );
//		bot3.update( sim );
//		bot4.update( sim );
//		game.getRenderer().setRenderAllPaths( true );
//		game.getRenderer().useGlow( false );
		try {
			Thread.sleep( 0 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	public void init(GLAutoDrawable arg0) {
		
	}

	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4) {
		// TODO Auto-generated method stub
		
	}
}
