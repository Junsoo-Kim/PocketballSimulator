package model;

import java.awt.*;

/**
 * 시뮬레이터 내에서 사용되는 상수 모음
 */
public class Constant {
    public static final Color BALL_COLOR[] = {Color.WHITE, Color.YELLOW, Color.RED, Color.PINK, Color.GREEN, Color.BLACK};
    public static final double HOLE_SIZE = 8f;

    //흰 공을 쳤을 때 가해지는 힘 배수. 공이 너무 빠르면 충돌이 제대로 되지 않을 수 있음
    public static final double POWER_UNIT = 0.1f;

    // 당구대의 반발 계수입니다. 0 초과 1 이하의 값
    public static final double TABLE_COR = 0.8;

    // 바닥 마찰에 의해 매 프레임 줄어드는 속력(등감속 모델, 단위: 좌표/프레임^2)입니다.
    // 실제 대회 클라이언트를 역분석한 커뮤니티 코드(예: v^2 = base + 2*mue*distance 형태의 식)를 보면
    // 속도가 매 프레임 일정 비율로 줄어드는 게 아니라, 일정한 크기만큼 감속하다가 멈추는
    // "등감속 마찰" 모델에 가깝습니다. 이 시뮬레이터도 그 방식을 따릅니다.
    public static final double FRICTION_DECEL = 0.2;

    // 홀(포켓)을 테이블 모서리/변에서 공 반지름의 이 비율만큼 안쪽으로 당겨 배치합니다.
    // 실제 클라이언트는 홀이 정확히 모서리 좌표에 있지 않고 살짝 안쪽에 있다고 알려져 있어
    // (커뮤니티에서 역분석한 값들이 0.15~0.3 사이로 제각각이라 정확한 공식 값은 아님) 근사치를 사용합니다.
    public static final double HOLE_INSET_RATIO = 0.2;

    //최대 파울 개수
    public static final int MAX_FOUL = 3;

    //플레이어 당 최대 턴 수. 모든 플레이어가 이 턴 수를 소진하면 게임이 종료되고,
    //파울이 더 적은 플레이어가 승리합니다(파울 개수가 같으면 후공이 승리).
    public static final int MAX_TURN = 20;

    public static final int FPS = 60;

    /**
     * Don't Touch Below!!!
     */
    public static final int SIZE_UNIT = 5;
    public static final int TABLE_WIDTH = 254;
    public static final int TABLE_HEIGHT = 127;
    public static final double VELOC_BOUND = 0.003f;
    public static final int SKIP_TICKS = 1000/FPS;
    public static final double MAX_POWER = 100f;
    public static final double MIN_POWER = 0f;
}
