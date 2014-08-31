package wwfsolver;

public interface IMoveSelector {
	public void move(String word, int row, int col, int score, boolean down);
}
