package group11;

import static group11.Model.*;

public class Pattern {

	private int num = 0;

	private Node root;

	public Pattern() {
		root = new Node(num);
	}

	public void add(int val, char dir) {
		Node node = new Node(++num, val);
		switch (dir) {
		case 'L': root.L = node; break;
		case 'R': root.R = node; break;
		case 'F': root.F = node; break;
		case 'B': root.B = node; break;
		default: break;
		}
	}

	public void moveTo(char dir) {

	}

	private class Node {

		private final int id;
		private int val;
		private Node L, R, F, B;

		public Node(int id) {
			this(id, EMPTY);
		}

		public Node(int id, int val) {
			this(id, val, null, null, null, null);
		}

		public Node(int id, int val, Node l, Node r, Node f, Node b) {
			this.id = id;
			this.val = val;
			L = l;
			R = r;
			F = f;
			B = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id != other.id)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Node [id=" + id + ", val=" + val + "]";
		}

		private Pattern getOuterType() {
			return Pattern.this;
		}

	}

}
