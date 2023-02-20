package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {
        long starttime = System.currentTimeMillis();//amount of milliseconds passed. 
        long endtime = starttime + 5000;
        int myMove=0;
        int  mr_max=currentBoard.getNextPlayer();
        int ms_min=0;
        int max_depth=1;
        int catch_move=0;
        if(mr_max==1)
        {
            ms_min=2;
 
        }
        else
        {
            ms_min=1;

        }
        while(System.currentTimeMillis()<endtime)//the depth of tree is continiously incremented. 
        {
           catch_move = get_best_move(currentBoard.clone(), 1, 1, max_depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, false,endtime,mr_max,ms_min);
           if(catch_move!=-1)
           {
               myMove=catch_move;
               
           }
           max_depth++;
        }
        return myMove;
    }
    
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
    
    public int get_best_move(GameState currentBoard, int ambo, int current_depth,int max_depth, int alpha, int beta, boolean maxturn, boolean minturn,long endtime,int mr_max,int ms_min)
    {
        int this_alpha=alpha;
        int this_beta=beta;
        int min_move=0;
        int max_move=0;
        int maxscore=0;
        int minscore=0;
        boolean max_has_an_extra_move=false; 
        boolean min_has_an_extra_move=false;
        boolean move_is_possible=false;
        int bestvalue=Integer.MIN_VALUE;
        int worstvalue=Integer.MAX_VALUE;
        int extra_move=0;
        int bestmove=1;
        
        
        GameState copyBoard=null;

            for(int i=ambo;i<7;i++)
            {
               if(System.currentTimeMillis()>=endtime)//return -1 if 5 seconds have passed. 
               {
                   return -1;
               }
               else if(currentBoard.gameEnded())//return 0 if your'e peble lands in a house but you don't have any more moves 
               {
                   return 0;
               }
                if(maxturn)
                {
                    
                    move_is_possible=currentBoard.moveIsPossible(i);
                    if(!move_is_possible)
                    {
                        continue;
                    }
                    copyBoard=currentBoard.clone();
                    copyBoard.makeMove(i);
                    max_has_an_extra_move = (copyBoard.getNextPlayer() == mr_max);
                    if(max_has_an_extra_move)
                    {
                        extra_move=get_best_move(copyBoard.clone(), 1, current_depth,max_depth, this_alpha, this_beta, true, false,endtime,mr_max,ms_min);
                        if(extra_move==-1)//if 5 seconds have passed exit 
                        {
                            return -1;
                        }
                        else if(extra_move!=0)
                        {
                          copyBoard.makeMove(extra_move);
                        }
                        
                    }
                    
                    if(current_depth<max_depth)
                    {
                        min_move=get_best_move(copyBoard.clone(), 1, current_depth+1,max_depth, this_alpha, this_beta, false, true,endtime,mr_max,ms_min);
                        if(min_move==-1)
                        {
                           return -1;
                        }
                        if(min_move!=0)
                        {
                           copyBoard.makeMove(min_move);
                        }
                    }
                     maxscore=copyBoard.getScore(mr_max);
                     minscore=copyBoard.getScore(ms_min);
                     maxscore=maxscore-minscore;
                     if(maxscore>bestvalue)
                     {
                       bestvalue=maxscore; 
                       bestmove=i;
                     }
                     if(bestvalue>this_alpha)
                     {
                       this_alpha=bestvalue;
                     }
                     if(this_alpha>=this_beta)
                     {
                       break;
                     }
                            
                        
                    
                    
                }
                if(minturn)
                {
   
                    move_is_possible=currentBoard.moveIsPossible(i);
                    if(!move_is_possible)
                    {
                        continue;
                    }
                    copyBoard=currentBoard.clone();
                    copyBoard.makeMove(i);
                    min_has_an_extra_move=(copyBoard.getNextPlayer()==ms_min);
                    if(min_has_an_extra_move)
                    {  
                        extra_move=get_best_move(copyBoard.clone(), 1, current_depth,max_depth, this_alpha, this_beta, false, true,endtime,mr_max,ms_min);
                        if(extra_move==-1)
                        {
                            return -1;
                        }
                        if(extra_move!=0)
                        {
                           copyBoard.makeMove(extra_move);
                        }
                       
                        minscore=extra_move;
                        if(minscore<=worstvalue)
                        {
                           worstvalue=minscore; 
                        }
                    }
                  
                    maxscore=copyBoard.getScore(mr_max);
                    minscore=copyBoard.getScore(ms_min);
                    maxscore=maxscore-minscore;
                    if(current_depth<max_depth){       
                       max_move=get_best_move(copyBoard.clone(), 1, current_depth+1,max_depth,this_alpha, this_beta,true,false,endtime,mr_max,ms_min);
                       if(max_move==-1)
                       {
                           return -1;
                       }
                       if(max_move!=0)
                       {
                          copyBoard.makeMove(max_move);
                       }
                       
                       maxscore=copyBoard.getScore(mr_max);
                       minscore=copyBoard.getScore(ms_min);
                       maxscore=maxscore-minscore;
                       
                    }
                     
                     
                     
                     if(maxscore<worstvalue)
                     {
                       worstvalue=maxscore; 
                       bestmove=i;
                     }
                     if(worstvalue<this_beta)
                     {
                        this_beta=worstvalue;
                     }
                     if(this_alpha>=this_beta)
                     {
                        break; 
                     }
                        
                }
                    
                
            }
        return bestmove;
    }
    
 
    
    
}