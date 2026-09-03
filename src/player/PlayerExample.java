package player;

import controller.Game;
import model.Ball;
import model.Constant;

/**
 * Player.java에 채워 넣을 로직의 참고용 예시입니다. Game은 이 클래스를 직접 쓰지 않으니,
 * 필요한 부분만 골라서 Player.java로 옮겨 쓰세요.
 */
public class PlayerExample {
	//플레이어의 순서
	private int order = 0;

	/*
	 각 공의 위치를 나타냅니다.
	 balls[0]은 수구(흰공), balls[balls.length - 1]은 검은 공입니다.
	 플레이어가 2명일 경우 order = 0이면 홀수, 1이면 짝수번 공이 목적구입니다(마지막 공 제외)
	*/
	private double[][] balls;

	private double power = Constant.MAX_POWER;
	private double angle = 0f;

	public PlayerExample(int order, double[][] balls){
		this.order = order;
		this.balls = balls;
	}

	//do not modify above
	//please modify below

	/**
	 * 실제 대회 후기에서 자주 언급되는 '이미지볼(고스트볼)' 조준법을 적용한 예시입니다.
	 * 1. 내 목적구마다, 그 공을 각 홀에 넣으려면 수구가 맞아야 할 가상의 공(이미지볼) 위치를 계산하고
	 * 2. 수구→이미지볼, 이미지볼→홀 경로에 다른 공이 걸쳐 방해가 되지는 않는지 확인해서
	 * 3. 방해구 없이 칠 수 있는 조합 중 가장 만만한(총 이동 거리가 짧은) 이미지볼을 조준점으로 고릅니다.
	 * 방해구 없이 넣을 수 있는 조합이 하나도 없다면, 일단 가장 가까운 목적구를 직선으로 노려봅니다.
	 * 이 로직은 예시일 뿐이니 각도 계산, 샷 선택 기준(각도가 작을수록 쉬운 샷이라는 점 등) 등을
	 * 스스로 더 다듬어 봅시다.
	 *
	 * 반환값은 이 시뮬레이터 좌표계 기준 실제 대회 클라이언트와 동일한 각도 체계를 따릅니다:
	 * 0도 = (+y) 방향, 시계 방향으로 증가. 그래서 여기서는 조준점까지의 방향을
	 * 표준 수학 각도(atan2, 0도 = +x, 반시계 증가)로 구한 뒤 toGameAngle()로 변환해서 반환합니다.
	*/
	public double getAngle() {
		double[] aimPoint = pickAimPoint();
		if (aimPoint == null) return angle = 0f;
		double mathAngle = getAngle(balls[0], aimPoint);
		return angle = toGameAngle(mathAngle);
	}

	/**
	 * 표준 수학 각도(0도 = +x, 반시계 방향 증가)를
	 * 실제 대회 클라이언트가 쓰는 각도 체계(0도 = +y, 시계 방향 증가)로 바꾼다.
	 * 이 변환은 자기 자신의 역함수라서(대입을 두 번 하면 원래 값으로 돌아옴) 반대 방향 변환에도 그대로 쓸 수 있다.
	 */
	private double toGameAngle(double mathAngle) {
		return (90 - mathAngle + 360) % 360;
	}

	public double getPower() {
		//짧게 끊어치면 공끼리 붙어있을 때 충돌 계산이 튀는 경우가 있어, 세게 치는 편이 안정적입니다.
		return power = Constant.MAX_POWER;
	}

	/**
	 * 칠 수 있는 (목적구, 홀) 조합들의 이미지볼 위치를 계산해, 그중 가장 나은 조준점을 고른다.
	 * @return 수구가 조준해야 할 좌표. 마땅한 조합이 없으면 null
	 */
	private double[] pickAimPoint() {
		double[] best = null;
		double bestScore = Double.MAX_VALUE;

		for (int i = 1; i < balls.length; i++) {
			if (!isObjectBall(i)) continue;

			for (double[] holePos : Game.HOLES) {
				double[] ghost = getGhostBallPos(balls[i], holePos);

				//수구 -> 이미지볼, 이미지볼 -> 홀 경로 중 하나라도 다른 공에 막히면 칠 수 없는 조합
				if (isBlocked(balls[0], ghost, i) || isBlocked(ghost, holePos, i)) continue;

				double score = getDist(balls[0], ghost) + getDist(balls[i], holePos);
				if (score < bestScore) {
					bestScore = score;
					best = ghost;
				}
			}
		}

		//방해구 없이 넣을 수 있는 조합이 없다면, 일단 가장 가까운 목적구라도 직선으로 노려본다.
		if (best == null) {
			double minDist = Double.MAX_VALUE;
			for (int i = 1; i < balls.length; i++) {
				if (!isObjectBall(i)) continue;
				double dist = getDist(balls[0], balls[i]);
				if (dist < minDist) {
					minDist = dist;
					best = balls[i];
				}
			}
		}

		return best;
	}

	/**
	 * 목적구를 홀에 넣기 위해 수구가 맞아야 하는 이미지볼(고스트볼) 위치를 계산한다.
	 * 목적구 중심을 기준으로, (홀 -> 목적구) 방향으로 두 공의 지름만큼 떨어진 자리다.
	 */
	private double[] getGhostBallPos(double[] target, double[] hole) {
		double dx = target[0] - hole[0];
		double dy = target[1] - hole[1];
		double dist = getDist(target, hole);
		if (dist == 0) return target;

		double ux = dx / dist;
		double uy = dy / dist;
		return new double[]{ target[0] + ux * Ball.DIAMETER, target[1] + uy * Ball.DIAMETER };
	}

	/**
	 * from -> to 직선 경로에 (수구, excludeIdx번 공을 제외한) 다른 유효한 공이 걸쳐 있는지 확인한다.
	 */
	private boolean isBlocked(double[] from, double[] to, int excludeIdx) {
		for (int i = 1; i < balls.length; i++) {
			if (i == excludeIdx) continue;
			if (isPotted(i)) continue;

			if (getPointToSegmentDist(balls[i], from, to) < Ball.DIAMETER) return true;
		}
		return false;
	}

	/**
	 * 점 p와 선분 ab 사이의 최단 거리를 구한다.
	 */
	private double getPointToSegmentDist(double[] p, double[] a, double[] b) {
		double abx = b[0] - a[0];
		double aby = b[1] - a[1];
		double abLenSq = abx * abx + aby * aby;

		double t = abLenSq == 0 ? 0 : ((p[0] - a[0]) * abx + (p[1] - a[1]) * aby) / abLenSq;
		t = Math.max(0, Math.min(1, t));

		double closestX = a[0] + abx * t;
		double closestY = a[1] + aby * t;
		return getDist(p, new double[]{ closestX, closestY });
	}

	private double getDist(double[] a, double[] b) {
		double xDiff = a[0] - b[0];
		double yDiff = a[1] - b[1];

		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	private double getAngle(double[] from, double[] to){
		double xDiff = to[0] - from[0];
		double yDiff = to[1] - from[1];

		return Math.toDegrees(Math.atan2(yDiff, xDiff));
	}

	/**
	 * 포켓된 공인지 확인한다. 실제 대회 클라이언트처럼, 포켓된 공의 좌표는 (-1, -1)로 온다.
	 */
	private boolean isPotted(int n){
		return balls[n][0] < 0;
	}

	private boolean isObjectBall(int n){
		if (isPotted(n)) return false;

		int ballCount = 0;
		for (int i = 1; i < balls.length - 1; i++){
			if (isPotted(i)) continue;
			if (order == 0 && i % 2 == 1) ballCount++;
			else if (order == 1 && i % 2 == 0) ballCount++;
		}

		if (ballCount == 0) return n == balls.length - 1;

		if (n == balls.length - 1) return false;

		if (order == 0) return n % 2 == 1;
		return n % 2 == 0;
	}
}
