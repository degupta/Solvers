package wwfsolver;

public class CrossCheck {
	boolean isDirty = true;

	// [wordScore, checks]
	int[] across = new int[] { 0, 0 };
	int[] down = new int[] { 0, 0 };

	public void resetClean() {
		this.across[0] = this.across[1] = this.down[0] = this.down[1] = 0;
		this.isDirty = false;
	}
}
