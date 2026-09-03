package model;

import controller.Game;

import java.awt.*;

public class Ball {
	public static final double MASS = 1;
	public static final double DIAMETER = 5.73f;

	//사실 private로 두고 getter, setter를 두는 게 낫겠지만 바꿀 게 너무 많아서 public으로 둠
    private double x;
	private double y;
	private double nx;
	private double ny;
	private double xVeloc;
	private double yVeloc;
	private double xa;
	private double ya;
	private boolean isValid = false;
	private Color color;

	public Ball(int idx, double x, double y)  {
        this.x = x;
		this.y = y;
		this.nx = x;
		this.ny = y;
		this.isValid = true;
		this.color = (idx == Game.Balls.length -1 ? Color.BLACK : Constant.BALL_COLOR[idx % 6]);
	}

	public void addPower(double power, double angle) {
		double acc = power / MASS;
		angle = Math.toRadians(angle);
		xa = Math.cos(angle) * acc;
		ya = Math.sin(angle) * acc;
		xVeloc = xa;
		yVeloc = ya;
	}

	public void calcNext() {
		if (!isValid) return;

		double speed = Math.sqrt(xVeloc * xVeloc + yVeloc * yVeloc);
		if (speed < Constant.VELOC_BOUND) {
			xVeloc = 0;
			yVeloc = 0;
		} else {
			//실제 클라이언트처럼, 속도에 비례해서가 아니라 매 프레임 일정한 크기만큼 감속한다(등감속 마찰).
			double nextSpeed = Math.max(0, speed - Constant.FRICTION_DECEL);
			xVeloc = xVeloc / speed * nextSpeed;
			yVeloc = yVeloc / speed * nextSpeed;
		}
		ny = y + yVeloc;
		nx = x + xVeloc;
	}

	public void setPos(double x, double y){
		this.x = x;
		this.y = y;
		this.nx = x;
		this.ny = y;
	}

	public void updatePos(){
		this.x = nx;
		this.y = ny;
	}

	public double getX(){
		return this.x;
	}

	public double getY(){
		return this.y;
	}

	//각 공들의 이동 속도가 최저 기준을 넘은 경우, 움직이고 있는 상태라 판단한다.
	public boolean isMoving(){
		return Math.abs(this.xVeloc) > Constant.VELOC_BOUND || Math.abs(this.yVeloc) > Constant.VELOC_BOUND;
	}

	public void setVeloc(double xVeloc, double yVeloc){
		this.xVeloc = xVeloc;
		this.yVeloc = yVeloc;
	}

	public void setValid(boolean isValid){
		this.isValid = isValid;
	}

	public boolean isValid(){
		return this.isValid;
	}

	public Color getColor(){
		return this.color;
	}

	// 아래는 물리 처리
	public boolean collides(Ball j) {
		if (!((j.nx - nx) * (j.nx - nx) + (j.ny - ny) * (j.ny - ny) < DIAMETER * DIAMETER)) return false;

		Vector ball1 = new Vector(new Point(x, y), new Point(nx, ny));
		Vector ball2 = new Vector(new Point(j.x, j.y), new Point(j.nx, j.ny));

		Vector vc = new Vector(ball1.p0, ball2.p0);
		Vector v3 = new Vector(ball1.p0, ball1.vx - ball2.vx, ball1.vy - ball2.vy);

		Vector vp = vc.getProjVector(v3.dx, v3.dy);
		Vector vn = new Vector(new Point(ball1.p0.x + vp.vx, ball1.p0.y + vp.vy), ball2.p0);

		if (DIAMETER > vc.getLength()) {
			double pen = DIAMETER - vc.getLength();
			ball1.p1.x -= vc.dx * pen;
			ball1.p1.y -= vc.dy * pen;
			Vector bv = bounce(ball1, ball2, vc);
			j.xVeloc = bv.vx;
			j.yVeloc = bv.vy;
			j.nx = j.x + j.xVeloc;
			j.ny = j.y + j.yVeloc;

		}
		else if (DIAMETER > vn.getLength()) {
			double moveBack = Math.sqrt(DIAMETER * DIAMETER - vn.getLength() * vn.getLength());
			Point p4 = new Point(vn.p0.x - moveBack * v3.dx, vn.p0.y - moveBack * v3.dy);
			Vector v4 = new Vector(ball1.p0, p4);

			if (v4.getLength() <= vc.getLength() && v4.dotProd(ball1.vx, ball1.vy) >= 0){
				double t = v4.getLength() / v3.getLength();

				ball1.p1 = new Point(ball1.p0.x + t * ball1.vx, ball1.p0.y + t * ball1.vy);
				ball2.p1 = new Point(ball2.p0.x + t * ball2.vx, ball2.p0.y + t * ball2.vy);

				Vector bv = bounce(ball1, ball2, new Vector(ball1.p1, ball2.p1));
				j.xVeloc = bv.vx;
				j.yVeloc = bv.vy;
			}
		}
		return true;
	}

	public Vector bounce(Vector tmp1, Vector tmp2, Vector v){
		Vector v1a = tmp1.getProjVector(v.dx, v.dy);
		Vector v1b = tmp1.getProjVector(v.dy, -v.dx);
		Vector v2a = tmp2.getProjVector(v.dx, v.dy);
		Vector v2b = tmp2.getProjVector(v.dy, -v.dx);

		Vector exchanged1 = v1b.add(v2a);
		Vector exchanged2 = v2b.add(v1a);

		xVeloc = exchanged1.vx;
		yVeloc = exchanged1.vy;

		nx = x + xVeloc;
		ny = y + yVeloc;

		return exchanged2;
	}

	public void collidesTable() {
		if (ny > Constant.TABLE_HEIGHT - DIAMETER/2){
            ny = Constant.TABLE_HEIGHT - DIAMETER/2;
            yVeloc *= -Constant.TABLE_COR;
        }
        else if (ny < DIAMETER/2){//천장 충돌
            ny = DIAMETER/2;
            yVeloc *= -Constant.TABLE_COR;
        }

        if (nx > Constant.TABLE_WIDTH - DIAMETER/2){
            nx = Constant.TABLE_WIDTH - DIAMETER/2;
            xVeloc *= -Constant.TABLE_COR;
        }
        else if (nx < DIAMETER/2){
            nx = DIAMETER/2;
            xVeloc *= -Constant.TABLE_COR;
		}
	}



}
