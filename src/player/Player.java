package player;

import controller.Game;
import model.Ball;
import model.Constant;

/**
 * 대회 참가자가 작성하는 "전략" 클래스입니다.
 *
 * Game은 이 플레이어의 차례가 되면 매번 getAngle() → getPower() 순서로 딱 한 번씩 호출해서,
 * 그 반환값 그대로 흰 공(수구)을 칩니다. 즉, 아래 두 메서드를 채우는 것이 이 시뮬레이터(그리고
 * 실제 대회)에서 여러분이 해야 할 일의 전부입니다.
 *
 * ▶ 지켜야 할 규칙
 * 1. getAngle()과 getPower()의 이름·매개변수·반환 타입은 바꾸면 안 됩니다. Game이 정확히 이
 *    시그니처로 호출하기 때문입니다. (생성자 Player(int, double[][])도 마찬가지입니다.)
 * 2. 두 메서드는 계산해서 값을 "반환"만 하면 됩니다. 예외를 던지거나 멈추면 안 됩니다.
 * 3. 이 인스턴스는 매 턴 새로 만들어지는 게 아니라 게임 내내 하나만 유지됩니다. 그래서 필드에
 *    상태를 저장해두고 다음 턴에 참고하는 것도 가능합니다(지금은 order, balls만 있지만
 *    필드를 자유롭게 추가해도 됩니다).
 */
public class Player {
	//이 플레이어의 순서(0 또는 1).
	//플레이어가 2명일 때 order == 0이면 홀수 번호 공이, order == 1이면 짝수 번호 공이 내 목적구입니다
	//(마지막 번호 공은 검은 공이라 제외). 두 목적구를 모두 넣은 뒤에만 검은 공이 목적구가 됩니다.
	private int order = 0;

	/*
	 매 순간 최신 공 위치가 담겨 들어오는 배열입니다.
	 - balls[0] : 흰 공(수구)
	 - balls[balls.length - 1] : 검은 공(마지막 목적구)
	 - balls[i][0]이 x좌표, balls[i][1]이 y좌표입니다.
	 - 포켓(홀)에 이미 들어가 무효화된 공은 좌표가 (-1, -1)로 옵니다. 테이블 안 좌표는 항상
	   0 이상이므로, balls[i][0] < 0 이면 그 공은 이미 포켓된 것으로 보면 됩니다.
	*/
	private double[][] balls;

	private double power = Constant.MAX_POWER;
	private double angle = 0f;

	public Player(int order, double[][] balls){
		this.order = order;
		this.balls = balls;
	}

	//do not modify above
	//please modify below

	/**
	 * 이번 턴에 흰 공을 칠 "각도"를 반환합니다. Game이 매 턴 정확히 한 번 호출합니다.
	 *
	 * ▶ 각도 규칙 (실제 대회 클라이언트와 동일한 체계입니다)
	 *   - 0도 = balls 좌표계 기준 (+y) 방향
	 *   - 각도는 "시계 방향"으로 증가합니다 → 90도 = (+x), 180도 = (-y), 270도 = (-x)
	 *   - 0 이상 360 미만 범위로 반환하세요.
	 *
	 * 만약 atan2 등으로 구한 "표준 수학 각도"(0도 = +x, 반시계 방향 증가)를 먼저 계산했다면,
	 * 아래 식으로 위 규칙에 맞게 변환해서 반환하면 됩니다.
	 *     realAngle = (90 - mathAngle + 360) % 360
	 *
	 * 힌트: this.balls로 모든 공의 현재 좌표를, controller.Game.HOLES로 6개 홀의 좌표를,
	 * model.Ball.DIAMETER로 공의 지름을 알 수 있습니다.
	 *
	 * @return 흰 공을 칠 각도 (0~360, 단위: 도)
	 */
	public double getAngle() {
		//TODO: 여기에 각도를 계산하는 로직을 작성하세요.
		return angle = 0;
	}

	/**
	 * 이번 턴에 흰 공을 칠 "세기"를 반환합니다. getAngle() 다음에 정확히 한 번 호출됩니다.
	 *
	 * ▶ 파워 규칙
	 *   - 0(= Constant.MIN_POWER) 이상 100(= Constant.MAX_POWER) 이하의 값이어야 합니다.
	 *   - 범위를 벗어나면 Game이 자동으로 0~100 사이로 잘라서 사용합니다.
	 *
	 * @return 흰 공을 칠 세기 (0~100)
	 */
	public double getPower() {
		//TODO: 여기에 파워를 계산하는 로직을 작성하세요.
		return power = Constant.MAX_POWER;
	}
}
