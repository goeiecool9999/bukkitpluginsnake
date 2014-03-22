package goeiecool9999.basicscoreboardgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Games extends JavaPlugin implements Listener{
public HashMap<Player,Snake> game = new HashMap<Player,Snake>();
public HashMap<Player,Scoreboard> boards = new HashMap<Player,Scoreboard>();
public Server serv = getServer();
public int snakedir;

	
	public void onEnable(){
		serv.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void Movement(PlayerMoveEvent event){
		Player player = event.getPlayer();
		
		
		
		
		boolean playerr;
		try{
			playerr = game.get(player).running;
		} catch (NullPointerException e) {
			playerr = false;
		}
		if(playerr){
			Location l = event.getFrom();
			l.setPitch(0);
			l.setYaw(0);
			event.getPlayer().teleport(l);
			Location from = event.getFrom();
			Location to = event.getTo();
			double dx = to.getX()-from.getX();
			double dz = to.getZ()-from.getZ();
			int curdir = game.get(player).snake.dir;
			if(Math.abs(dx) > Math.abs(dz)){
				if(dx > 0 && curdir != 0){
					snakedir = 2;
				}
				if(dx < 0 && curdir != 2){
					snakedir = 0;
				}
			}
			if(Math.abs(dz) > Math.abs(dx)){
				if(dz > 0 && curdir != 1){
					snakedir = 3;
				}
				if(dz < 0 && curdir != 3){
					snakedir = 1;
				}
			}
			game.get(player).snake.lastmove = snakedir;
			
		}
	}
	
	@EventHandler
	public void Disconnect(PlayerQuitEvent event){
		Player p = event.getPlayer();
		game.remove(p);
		boards.remove(p);
	}
	
	
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(command.getName().equalsIgnoreCase("snake")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				Snake snake = game.get(player);
				if(snake == null){
					Snake obj = new Snake(player);
					game.put(player, obj);
					snake = game.get(player);
				}
				
				if(snake.running){
					snake.stop();
					return true;
				}
				
				if(snake.gameover){
					snake.restart();
				}
				
				snake.stop();
				snake.setPlayer(player);
				snake.start();
				
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}
	
	
	public class Snake{
		public int task;
		public boolean running = false;
		public Player player;
		public SnakeRunnable snake;
		public boolean gameover = false;
		public Snake(Player playera){
			player = playera;
			snake = new SnakeRunnable(player);
		}
		
		public void setPlayer(Player p){
			player = p;
		}
		
		public void createBoard(){
			if(boards.get(player) == null){
				Scoreboard board = serv.getScoreboardManager().getNewScoreboard();
				boards.put(player, board);
			}
		}
		
		public void restart(){
			snake.restart();
			gameover = false;
			running = true;
		}
		
		public void start(){
			createBoard();
			player.setScoreboard(boards.get(player));
			snake.setPlayer(player);
			task = serv.getScheduler().scheduleSyncRepeatingTask(serv.getPluginManager().getPlugin("BasicScoreboardGames"), snake, 0, 10);
			gameover = false;
			running = true;
		}
		
		public void stop(){
			if((Integer)task != null){
				serv.getScheduler().cancelTask(task);
			}
			try{
				player.setScoreboard(serv.getScoreboardManager().getMainScoreboard());
			} catch (NullPointerException e){
				
			}
			running = false;
		}
	}
	
	public class ScoreboardPixelator{
		public String[] lines;
		public int dimensions;
		public char onChar = 'X';
		public char offChar = '_';
		public Scoreboard sb;
		public Server serv;
		public void clear(){
			for(int i = 0;i<lines.length; i++){
				char[] arr = new char[dimensions];
				for(int j = 0;j<dimensions;j++){
					arr[j] = offChar;
				}
				lines[i] = String.copyValueOf(arr);
			}
		}
		
		public void setTitle(String t){
			sb.getObjective("snake").setDisplayName(t);
		}
		
		public void initObjective(){
			try{
			sb.getObjective("snake").unregister();
			} catch (NullPointerException e){
				
			}
			Objective ob = sb.registerNewObjective("snake", "dummy");
			ob.setDisplayName("Snake");
			ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		public void setScoreboard(Scoreboard sc){
			sb = sc;
			initObjective();
			clear();
			updateScreen();
		}
		
		public ScoreboardPixelator(Scoreboard sba,Server serva){
			sb = sba;
			serv = serva;
			dimensions = 14;
			lines = new String[dimensions];
		}
		
		public void drawString(String s, int x, int y){
			char[] ga = s.toCharArray();
			for(int i = 0;i<ga.length;i++){
				if(ga[i] != ' '){
					setPixel(ga[i], x+i, y, false);
				}
			}
		}
		
		
		public boolean getPixel(int x, int y){
			try{
				String line = lines[y];
				if(line.charAt(x) == onChar){
					return true;
				} else {
					return false;
				}
				
				
			} catch(IndexOutOfBoundsException e){
				return false;
			}
		}
		
		public void setPixel(boolean on,int x,int y,boolean updatescreen){
			try{
				String line = lines[y];
				char[] arr = line.toCharArray();
			
				if(on){
					arr[x] = onChar;
				} else {
					arr[x] = offChar;
				}
	
				line = String.copyValueOf(arr);
				lines[y] = line;
				if(updatescreen){
					updateScreen();
				}
				
			} catch (IndexOutOfBoundsException e){
				
			}
		}
		
		public void setPixel(char c,int x,int y,boolean updatescreen){
			try{
				String line = lines[y];
				char[] arr = line.toCharArray();
			
				arr[x] = c;
	
				line = String.copyValueOf(arr);
				lines[y] = line;
				if(updatescreen){
					updateScreen();
				}
				
			} catch (IndexOutOfBoundsException e){
				
			}
		}
		
		public void setLine(int line,String text){
			line++;
			line = (dimensions+1)-line;
			OfflinePlayer lineplayer = serv.getOfflinePlayer(text+line);
			sb.getObjective("snake").getScore(lineplayer).setScore(line);
		}
		
		public void updateScreen(){
			for(OfflinePlayer p : sb.getPlayers()){
				sb.resetScores(p);
			}
			for(int i = 0;i<lines.length; i++){
				setLine(i,lines[i]);
			}
		}
	}
	
	
	public class SnakeRunnable implements Runnable{
		private Player player;
		private ScoreboardPixelator pix;
		private ArrayList<SnakeSegment> segs = new ArrayList<SnakeSegment>();
		private ArrayList<SnakeSegment> segsdead = new ArrayList<SnakeSegment>();
		private int sx = 0;
		private int sy = 0;
		private int cx = -1;
		private int cy = -1;
		private int lvl = 5;
		private int score = 0;
		private boolean gameover = false;
		public int dir;
		public int lastmove;
		private Random randomizer = new Random();
		public SnakeRunnable(Player p){
			super();
			player = p;
			pix = new ScoreboardPixelator(boards.get(player),serv);
			resetSnakepos();
		}
		
		public boolean isPointInSnake(int x, int y){
			boolean inside = false;
			for(SnakeSegment seg:segs){
				if(seg.x == x && seg.y == y){
					inside = true;
				}
			}
			return inside;
		}
		
		public void relocCandyOutsideSnake(){
			cx = randomizer.nextInt(pix.dimensions);
			cy = randomizer.nextInt(pix.dimensions);
			while(isPointInSnake(cx,cy)){
				cx = randomizer.nextInt(pix.dimensions);
				cy = randomizer.nextInt(pix.dimensions);
			}
		}
		public void relocCandy(){
			cx = randomizer.nextInt(10);
			cy = randomizer.nextInt(10);
		}
		
		public void resetSnakepos(){
			sx = (3/10)*pix.dimensions;
			sy = pix.dimensions/2;
		}
		
		public void restart(){
			resetSnakepos();
			cx = -1;
			cy = -1;
			lvl = 5;
			dir = 0;
			score = 0;
			gameover = false;
			segs.clear();
			segsdead.clear();
		}
		
		public void gameover(){
			gameover = true;
			game.get(player).gameover = true;
			game.get(player).running = false;
		}
		
		public void incScore(){
			score++;
			pix.setTitle("Snake points: " + score);
		}
		
		
		public void logic(){
			dir = lastmove;
			
			if(cx == -1 && cy == -1){
				relocCandy();
				dir = 0;
			}
			
			
			if(dir == 0){
				if(sx+1 < pix.dimensions){
					sx++;
				} else {
					gameover();
				}
			} else if(dir == 1){
				if(sy+1 < pix.dimensions){
					sy++;
				} else {
					gameover();
				}
			} else if(dir == 2){
				if(sx-1 >= 0){
					sx--;
				} else {
					gameover();
				}
			} else if(dir == 3){
				if(sy-1 >= 0){
					sy--;
				} else {
					gameover();
				}
			}
			
			if(sx == cx && sy == cy){
				lvl++;
				incScore();
				relocCandyOutsideSnake();
			}
			
			for(SnakeSegment seg : segs){
				
				seg.age--;
				if(seg.age <= 0){
					segsdead.add(seg);
				}
			}
			for(SnakeSegment seg: segsdead){
				segs.remove(seg);
			}
			segsdead.clear();
			
			for(SnakeSegment seg : segs){
				if(sx == seg.x && sy == seg.y){
					gameover();
				}
			}
			
			SnakeSegment s = new SnakeSegment(sx,sy,lvl);
			segs.add(s);
		}
		
		public void run(){
			
			if(!gameover){
				logic();
			}
			
			pix.clear();
			if(!gameover){
				for(SnakeSegment seg:segs){
					pix.setPixel(true, seg.x, seg.y, false);
				}
				pix.setPixel(true, cx, cy, false);
			} else {
				pix.drawString("game over", 0, pix.dimensions/2);
				pix.drawString("/snake to", 0, pix.dimensions/2+2);
				pix.drawString("restart", 1, pix.dimensions/2+3);
			}
			pix.updateScreen();
			

			
			
		}
		
		public void setPlayer(Player p){
			player = p;
			pix.setScoreboard(boards.get(player));
		}
		
	}
	
	public class SnakeSegment{
		public int age;
		public int x;
		public int y;
		
		public SnakeSegment(int xp,int yp,int agep){
			x = xp;
			y = yp;
			age = agep;
		}
	}


}
