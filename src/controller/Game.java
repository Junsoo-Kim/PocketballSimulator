package controller;

import model.Ball;
import model.Constant;
import player.Player;
import view.Display;

import java.awt.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Game {
	public static Ball[] Balls;
	public static double[][] balls; //0번째는 큐 볼. balls[][0]은 x좌표, [1]은 y좌표, 마지막 번호의 공이 검은 공

	public Toolkit toolkit = Toolkit.getDefaultToolkit();

	//실제 클라이언트는 홀이 테이블 모서리/변의 정확한 좌표가 아니라 공 반지름의 일정 비율만큼
	//안쪽으로 들어와 있는 것으로 알려져 있어(Constant.HOLE_INSET_RATIO), 그 형태를 흉내낸다.
	public static final double[][] HOLES = buildHoles();

	private static double[][] buildHoles(){
		double k = Ball.DIAMETER / 2 * Constant.HOLE_INSET_RATIO;
		double w = Constant.TABLE_WIDTH;
		double h = Constant.TABLE_HEIGHT;
		return new double[][] {
				{ k, k }, { w / 2, k / 2 }, { w - k, k },
				{ k, h - k }, { w / 2, h - k / 2 }, { w - k, h - k }
		};
	}

    private final int playerCount;

	private int order = 0;
	private int turnCount = 0;
	private boolean isPlaying;
	private boolean pocketNotObject = false;

	private int[] fouls;
	private final int[] playerBallCount;
	private int[] turnsTaken;

	private LocalTime time;

	private Player[] players;
	private final Display display;

	public Game(int playerCount, int ballCountForEachPlayer){
		this.playerCount = playerCount;

		//공 생성
		generateBalls(ballCountForEachPlayer);

		//새로운 플레이어 생성
		generatePlayers(playerCount);

		//디스플레이 생성
		this.display = new Display("Pocket model.Ball", Constant.TABLE_WIDTH, Constant.TABLE_HEIGHT, Constant.SIZE_UNIT);
		this.display.setBalls(Balls);

		//게임의 상태 초기화
		order = 0;
		isPlaying = true;
		turnCount = 2;
		playerBallCount = new int[playerCount];
		for (int i = 0; i < playerCount; i++) playerBallCount[i] = ballCountForEachPlayer + 1;
		turnsTaken = new int[playerCount];

		//시작 메시지 출력
		printStartMessage();

		//게임 시작
		play();
	}

	/**
	 * 임의의 위치에 공을 생성
	 */
	private void generateBalls(int ballCountForEachPlayer) {
		balls = new double[ballCountForEachPlayer * playerCount + 2][2];
		time = LocalTime.now();

		//흰 공의 위치 설정
		balls[0][0] = 254f/4;
		balls[0][1] = 127f/2;
		//검은 공의 위치 설정
		balls[balls.length-1][0] = 254f/4*3;
		balls[balls.length-1][1] = 127f/2;
		
		for (int i = 1; i < balls.length - 1; i++) {
			double rand = Math.random();
			balls[i][0] = rand * (Constant.TABLE_WIDTH - 15) + 6;
			rand = Math.random();
			balls[i][1] = rand * (Constant.TABLE_HEIGHT - 15) + 6;
			if (prevBallCollision(i) || tableCollision(i)) i--; 
		}

		Balls = new Ball[balls.length];
		for (int i = 0; i < balls.length; i++) {
			Balls[i] = new Ball(i, balls[i][0], balls[i][1]);
		}
	}

	/**
	 * 플레이어 생성
	 * @param playerCount 만들 플레이어의 수
	 */
	private void generatePlayers(int playerCount){
		players = new Player[playerCount];
		fouls = new int[playerCount];
		for (int i = 0; i < playerCount; i++) players[i] = new Player(i, balls);
	}

	/**
	 *	시작 메시지를 출력
	 */
	private void printStartMessage(){
		System.out.println("----------------  게  임  시  작  -----------------");
		for (int i = 0; i < Balls.length; i++){
			if (i == Balls.length - 1) {
				System.out.println("    " + i + "번 공의 색은 검정");
				continue;
			}
			switch (i){
				case 0: System.out.print("    " + i + "번 공의 색은 흰색"); break;
				case 1: System.out.print("    " + i + "번 공의 색은 노랑"); break;
				case 2: System.out.print("    " + i + "번 공의 색은 빨강"); break;
				case 3: System.out.print("    " + i + "번 공의 색은 분홍"); break;
				case 4: System.out.print("    " + i + "번 공의 색은 초록"); break;
				case 5: System.out.print("    " + i + "번 공의 색은 검정"); break;
			}
			if (i % 2 == 1) System.out.println();
		}
		System.out.println("-------------------------------------------------");
	}

	private boolean prevBallCollision(int cnt) {
		for (int i = 0; i < cnt; i++) {
			if (getDist(balls[i], balls[cnt]) < Ball.DIAMETER) return true;
		}
		return getDist(balls[cnt], balls[balls.length-1]) < Ball.DIAMETER;
	}

	private boolean tableCollision(int i) {
		if (balls[i][0] > Constant.TABLE_WIDTH - Ball.DIAMETER/2) return true;
		if (balls[i][0] < Ball.DIAMETER/2) return true;
		if (balls[i][1] < Ball.DIAMETER/2) return true;
		return balls[i][1] > Constant.TABLE_HEIGHT - Ball.DIAMETER/2;
	}
	
	private double getDist(double []a, double[]b) {
		return Math.sqrt((a[0] - b[0])*(a[0] - b[0]) + (a[1] - b[1])*(a[1] - b[1]));
	}

	/**
	 * 게임 플레이 로직
	 */
	private void play() {
		while (isPlaying) {
			time = LocalTime.now();
			System.out.println("[" + (order + 1) +"번 플레이어의 " + (turnCount/2) +"번째 차례]");
			for (int i = 0; i < playerCount; i++){
				System.out.println((i + 1) + "번 플레이어의 남은 공 수: " + playerBallCount[i]);
			}

			//흰 공을 넣어버린 경우
			if (!Balls[0].isValid()) {
				System.out.println("흰 공을 넣었으므로 흰 공의 위치를 재설정하고 게임을 재진행합니다.");

				//0.5초의 텀을 줌
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				Balls[0].setValid(true);
				Balls[0].setPos((double) Constant.TABLE_WIDTH / 2, (double) Constant.TABLE_HEIGHT / 2);
				balls[0][0] = Balls[0].getX();
				balls[0][1] = Balls[0].getY();
			}

			//플레이어로부터 힘과 각도를 받아서
			turnsTaken[order]++; //이번 턴을 소진한 것으로 기록

			double angle = players[order].getAngle();
			double power = players[order].getPower();

			//Player가 반환하는 각도는 실제 대회 클라이언트와 같은 체계다(0도 = +y 방향, 시계 방향으로 증가).
			//내부 물리 연산은 표준 수학 각도(0도 = +x 방향, 반시계 방향으로 증가)를 쓰므로 변환해준다.
			angle = (90 - angle + 360) % 360;

			//최대 & 최소 힘 내로 설정
			if (power > Constant.MAX_POWER) power = Constant.MAX_POWER;
			if (power < Constant.MIN_POWER) power = Constant.MIN_POWER;
			power *= Constant.POWER_UNIT;

			//수구에 힘과 각도를 넣기
			Balls[0].addPower(power, angle);

            boolean isMoving = true;
			int whiteFirstHitIdx = 0; //수구가 가장 먼저 친 공의 번호
			int pocket = 0;
			boolean continueOrder = false;//순서가 안 바뀌고 그대로 진행할지 여부

			pocketNotObject = false;

			//움직이는 공이 있는 경우
			while (isMoving) {
				isMoving = false;

				//FPS
				if (time.until(LocalTime.now(), ChronoUnit.MILLIS) < Constant.SKIP_TICKS) {
					isMoving = true;
					continue;
				}

				for (int i = 0; i < balls.length; i++) {
                    if (Balls[i].isMoving()){
                        isMoving = true;
                        break;
                    }
				}

				//수구가 가장 먼저 친 공을 확인
				int hit = tryMove();
				if (whiteFirstHitIdx == 0) whiteFirstHitIdx = hit;

				//게임 상태 업데이트
				int u = update();

				if (pocket == 0) pocket = u;
				else if (u < 0 && pocket > 0) pocket = u;

				display.draw();
				time = LocalTime.now();
				toolkit.sync();
			}


			if (isPlaying) {
				//파울 처리
				//아무 공도 못 맞히거나, 목적구가 아닌 공을 먼저 맞히거나, 목적구가 아닌 공을 넣거나
				if (whiteFirstHitIdx == 0) {
					fouls[order]++;
					System.out.println("아무 공도 맞히지 못했습니다. 파울(" + fouls[order] + ")");
				} else if (!isObjectBall(whiteFirstHitIdx)) {
					fouls[order]++;
					System.out.println("목적구가 아닌 공(" + whiteFirstHitIdx + "번)을 먼저 맞혔습니다. 파울(" + fouls[order] + ")");
				} else if (pocketNotObject) {
					fouls[order]++;
					System.out.println("목적구가 아닌 공을 포켓했습니다. 파울(" + fouls[order] + ")");
				}

				if (fouls[order] == Constant.MAX_FOUL) {
					System.out.println((order + 1) + "번 플레이어가 "+ Constant.MAX_FOUL +"번의 파울로 패배했습니다.");
					isPlaying = false;
				}

				//목적구를 넣은 경우, 자신의 턴을 계속함
				if (pocket > 0) continueOrder = true;

				for (int i = 0; i < playerCount; i++){
					if (playerBallCount[i] == 0){
						System.out.println((i + 1) + "번 플레이어의 승리.");
						isPlaying = false;
					}
				}

				//아직 승부가 안 났다면, 모든 플레이어가 최대 턴 수를 소진했는지 확인
				if (isPlaying && allPlayersReachedMaxTurn()) endByTurnLimit();

				if (isPlaying && !continueOrder) setNextOrder();
			}

			System.out.println("-------------------------------------------------");
		}
		System.out.println("게임 종료.");
	}

	private void setNextOrder(){
		order = (order + 1) % playerCount;
		turnCount += 1;
	}

	/**
	 * 모든 플레이어가 최대 턴 수(Constant.MAX_TURN)를 소진했는지 확인
	 */
	private boolean allPlayersReachedMaxTurn(){
		for (int taken : turnsTaken) {
			if (taken < Constant.MAX_TURN) return false;
		}
		return true;
	}

	/**
	 * 턴 제한에 도달해 게임을 종료. 파울이 더 적은 플레이어가 승리하며,
	 * 파울 개수가 같다면 후공(더 늦은 순서의 플레이어)이 승리한다.
	 */
	private void endByTurnLimit(){
		System.out.println("모든 플레이어가 " + Constant.MAX_TURN + "턴을 모두 소진했습니다.");

		int minFoul = Integer.MAX_VALUE;
		for (int f : fouls) minFoul = Math.min(minFoul, f);

		int tiedCount = 0;
		int winner = -1;
		for (int i = 0; i < playerCount; i++) {
			if (fouls[i] == minFoul) {
				tiedCount++;
				winner = i; //동률인 경우 인덱스가 더 큰(후공) 플레이어가 최종적으로 남음
			}
		}

		System.out.println((winner + 1) + "번 플레이어가 파울 " + fouls[winner] + "회로 승리했습니다"
				+ (tiedCount > 1 ? " (파울 동률로 후공 승)." : "."));
		isPlaying = false;
	}

	/**
	 * 공의 다음 위치를 계산해봄
	 * @return 수구로 가장 먼저 맞힌 공의 번호
	 */
	private int tryMove() {
		int firstHit = 0;
		for (int i = 0; i < balls.length; i++) 	{
			if (Balls[i].isValid()) Balls[i].calcNext();
		}

		for (int i = 0; i < balls.length; i++) {
			if (!Balls[i].isValid()) continue;
			for (int j = 0; j < balls.length; j++) {
				if (i == j) continue;
				if (!Balls[j].isValid()) continue;
				if (Balls[i].collides(Balls[j])) {
					if ((i == 0 || j == 0) && firstHit == 0) {
						firstHit = i + j;
					}
				}
				Balls[i].collidesTable();
			}
		}
		return firstHit;
	}

	/**
	 * 계산한 다음 상태로 게임의 상태를 업데이트
	 * @return -1 : 잘못된 공을 넣은 경우, 0 : 아무 것도 못 넣은 경우, 1: 목적구를 넣은 경우
	 */
	private int update() {
		int pocket = 0;
		for (int i = 0; i < Balls.length; i++) {
			Balls[i].updatePos();
			balls[i][0] = Balls[i].getX();
			balls[i][1] = Balls[i].getY();

			int h = checkHoles(i);

			if (pocket == 0) pocket = h;
			else if (h < 0 && pocket > 0) pocket = -1;
		}
		return pocket;
	}

	/**
	 * 해당 번호의 공이 현재 순서의 플레이어의 목적구인지 확인
	 * @param ballNum 공의 번호
	 * @return 목적구 여부
	 */
	private boolean isObjectBall(int ballNum){
		if (ballNum == 0) return false;
		if (playerCount == 2) {
			if (ballNum % 2 != order && ballNum != Balls.length - 1) return true;
		}
		else if (ballNum != Balls.length - 1) return true;
		return playerBallCount[order] <= 1 && ballNum == Balls.length-1;
	}

	/**
	 * 각 공이 홀에 들어갔는지 여부를 판단
	 * @param idx 공의 번호
	 * @return -1 목적구가 아닌 공을 넣음, 0 아무 것도 안 넣음, 1 목적구 넣음
	 */
	private int checkHoles(int idx){
		if (!Balls[idx].isValid()) return 0;
		double x = Balls[idx].getX();
		double y = Balls[idx].getY();

        for (double[] hole : HOLES) {
            if (getDist(new double[]{x, y}, hole) < Constant.HOLE_SIZE) {
				System.out.printf("%d번 공 포켓!!\n", idx);

				//invalidate. 실제 클라이언트처럼 포켓된 공은 (0, 0)이 아니라 테이블 밖을 뜻하는 (-1, -1)로 표시한다.
                Balls[idx].setValid(false);
				Balls[idx].setVeloc(0, 0);
				Balls[idx].setPos(-1, -1);
				balls[idx][0] = balls[idx][1] = -1;

				//목적구가 아닌 경우
                if (!isObjectBall(idx)) {
                    if (idx == Balls.length - 1 && playerBallCount[order] != 1) {
                        System.out.printf("공이 남아있는데 마지막 공을 넣었으므로 %d번 플레이어의 패배입니다.\n", order);
                        isPlaying = false;
                    } else if (idx != 0) {//상대편의 볼 카운트를 줄여 줌
						pocketNotObject = true;
						if (playerBallCount[playerCount - order - 1] > 0) playerBallCount[playerCount - order - 1]--;
                    }
                    return -1;
                }
                System.out.println("목적구를 넣는 데 성공했습니다.");
				if (playerBallCount[order] > 0) playerBallCount[order]--;
                return 1;
            }
        }
		return 0;
	}
}
