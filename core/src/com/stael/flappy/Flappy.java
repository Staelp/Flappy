package com.stael.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import sun.rmi.runtime.Log;

public class Flappy extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture player;
	Texture playerJump;
	Texture pipeTop;
	Texture pipeBottom;
	int playerHeight = 0;
	int playerXCoord = 0;
	float gravity = 0.2f;
	float velocity = 0;
	float min = 0.45f;
	float max = 0.75f;
	float gap;
	int pipeCount = 0;
	ArrayList<Integer> pipeXs;
	ArrayList<Integer> gapXs;
	ArrayList<Float> pipeTopLength;
	ArrayList<Float> pipeBottomLength;
	Random rand;
	Timer timer;
	int score = 0;
	long between;
	int gameState = 0;

	Rectangle playerRect;
	ArrayList<Rectangle> pipeTopRect;
	ArrayList<Rectangle> pipeBottomRect;
	ArrayList<Rectangle> gapRect;

	BitmapFont font;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");
		player = new Texture("bird.png");
		playerJump = new Texture("bird2.png");
		playerHeight = Gdx.graphics.getHeight()/2 - player.getHeight()/2;

		pipeBottom = new Texture("bottomtube.png");
		pipeTop = new Texture("toptube.png");
		pipeXs = new ArrayList<Integer>();
		gapXs = new ArrayList<Integer>();
		pipeTopLength = new ArrayList<Float>();
		pipeBottomLength = new ArrayList<Float>();
		playerRect = new Rectangle();
		pipeTopRect = new ArrayList<Rectangle>();
		pipeBottomRect = new ArrayList<Rectangle>();
		gapRect = new ArrayList<Rectangle>();

		rand = new Random();
		playerXCoord = Gdx.graphics.getWidth() / 2 - player.getWidth() / 2;
		font = new BitmapFont();
		font.getData().setScale(3);

	}

	public void makePipes () {
		//To randomize the height of the pipes to challenge the player
		float height = min + rand.nextFloat() * (max - min);

		//Gap between pipes will be roughly a quarter of the screen
		gap = Gdx.graphics.getHeight()/4.2f;
		float bottomHeight = height * -Gdx.graphics.getHeight();

		//y = screen height + bottom height + gap
		//y - screen height - bottom height = gap
		pipeTopLength.add(Gdx.graphics.getHeight() + bottomHeight + gap);
		pipeBottomLength.add(bottomHeight);
		pipeXs.add(Gdx.graphics.getWidth());
		gapXs.add(Gdx.graphics.getWidth());
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if(gameState == 1) {

			if (pipeCount < 150) {
				pipeCount++;
			} else {
				pipeCount = 0;
				makePipes();
			}

			pipeBottomRect.clear();
			pipeTopRect.clear();
			gapRect.clear();
			for (int i = 0; i < pipeXs.size(); i++) {
				batch.draw(pipeTop, pipeXs.get(i), pipeTopLength.get(i));
				batch.draw(pipeBottom, pipeXs.get(i), pipeBottomLength.get(i));
				pipeXs.set(i, pipeXs.get(i) - 7);
				gapXs.set(i, gapXs.get(i) - 7);

				pipeTopRect.add(new Rectangle(pipeXs.get(i), pipeTopLength.get(i), pipeTop.getWidth(), pipeTop.getHeight()));
				pipeBottomRect.add(new Rectangle(pipeXs.get(i), pipeBottomLength.get(i), pipeBottom.getWidth(), pipeBottom.getHeight()));

				//Sets the rectangle height to the entirety of the screen so scoring can happen when passing through the gap
				//This can also happen when the player hits the pipe, but an if statement about gameState will block that
				gapRect.add(new Rectangle(gapXs.get(i), 0, pipeBottom.getWidth()/4, Gdx.graphics.getHeight()));
			}

			//Making sure the player icon never falls below the screen
			if (playerHeight <= 0) {
				playerHeight = 0;
				gameState = 2;
			}
			//Jumping
			if (Gdx.input.justTouched()) {
				velocity = 0;
				velocity -= 7;

			}

			velocity += gravity;
			playerHeight -= velocity;

			//Making it so that the wings flap everytime the user presses jump
			if (velocity < -4.7) {
				batch.draw(playerJump, playerXCoord, playerHeight);
			} else {
				batch.draw(player, playerXCoord, playerHeight);
			}

			playerRect.set(playerXCoord, playerHeight, player.getWidth(), player.getHeight());
			for (int i = 0; i < pipeXs.size(); i++) {
				if (Intersector.overlaps(playerRect, pipeBottomRect.get(i)) ||
						Intersector.overlaps(playerRect, pipeTopRect.get(i))) {
					Gdx.app.log("Collision", "You died!");
					Gdx.app.log("Location Data", pipeTopLength.get(i) + ", " + playerHeight + ", " + pipeBottomLength.get(i));

					gameState = 2;
				}

				//Adding the condition, i == score, solves the problem where score increased multiple times per pipe
				if(Intersector.overlaps(playerRect, gapRect.get(i)) && gameState != 2 && i == score) {
					score++;
					Gdx.app.log("Score",  Integer.toString(score));
				}
			}
			font.draw(batch, String.valueOf(score), 100, 200);
		} else if(gameState == 0) {
			batch.draw(player, Gdx.graphics.getWidth()/2 - player.getWidth()/2, Gdx.graphics.getHeight()/2 - player.getHeight()/2);
			if(Gdx.input.justTouched()) {
				pipeCount = 0;
				pipeXs.clear();
				gapXs.clear();
				pipeTopLength.clear();
				pipeBottomLength.clear();
				pipeTopRect.clear();
				pipeBottomRect.clear();
				gapRect.clear();
				playerHeight = Gdx.graphics.getHeight()/2 - player.getHeight()/2;
				score = 0;
				gameState = 1;
			}
		} else if(gameState == 2) {
			batch.draw(player, Gdx.graphics.getWidth()/2 - player.getWidth()/2, playerHeight);
			font.draw(batch, "You died", Gdx.graphics.getWidth()/2.5f, Gdx.graphics.getHeight()*.80f);
			if(Gdx.input.justTouched()) {
				gameState = 0;
			}
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		background.dispose();

	}
}
