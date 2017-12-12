
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/**
 * An implementation of A* algorithm.
 *
 * @author Cong Bao
 */
public class AStar {

	private GridWorldModel model;

	/**
	 * Constructor of {@link AStar}.
	 *
	 * @param model
	 *            an instance of {@link GridWorldModel}
	 */
	public AStar(GridWorldModel model) {
		this.model = model;
	}

	/**
	 * Find a path between two points.
	 *
	 * @param from
	 *            the start point
	 * @param to
	 *            the end point
	 * @return a list of {@link Location} contains the points in the found path
	 *         (expect the start point)
	 */
	public List<Location> findPath(Location from, Location to) {
		List<Node> openList = new LinkedList<>();
		List<Node> closeList = new LinkedList<>();
		Node start = new Node(from);
		Node end = new Node(to);
		Node point = start;
		closeList.add(point);
		boolean flag = false;
		while (!flag) {
			for (Node next : point.getSurrounds()) {
				if (isOutOfBounds(next)) {
					continue;
				}
				if (next.equals(end)) {
					end.parent = point;
					flag = true;
					break;
				}
				if (closeList.contains(next)) {
					continue;
				}
				if (openList.contains(next)) {
					if (point.g + 1 < next.g) {
						next.parent = point;
						next.g++;
						next.f = next.g + next.h;
					}
					continue;
				}
				if (!isOccupied(next)) {
					next.parent = point;
					next.g++;
					next.h = getDistance(point, next);
					next.f = next.g + next.h;
					openList.add(next);
				}
			}
			if (flag) {
				break;
			}
			if (openList.isEmpty()) {
				return new LinkedList<>();
			}
			point = Collections.min(openList);
			openList.remove(point);
			closeList.add(point);
		}
		List<Location> path = new LinkedList<>();
		point = end;
		while (point.parent != null) {
			path.add(point.toLocation());
			point = point.parent;
		}
		Collections.reverse(path);
		return path;
	}

	private int getDistance(Node node1, Node node2) { // Manhattan
		return Math.abs(node1.x - node2.x) + Math.abs(node1.y - node2.y);
	}

	private boolean isOutOfBounds(Node node) {
		if (node.x < 0 || node.y < 0) {
			return true;
		}
		if (node.x >= model.getWidth() || node.y >= model.getHeight()) {
			return true;
		}
		return false;
	}

	private boolean isOccupied(Node node) {
		return !model.isFreeOfObstacle(node.x, node.y);
	}

	private class Node implements Comparable<Node> {

		private int x;
		private int y;

		private int g;
		private int h;
		private int f;

		private Node parent;

		/**
		 * Constructor of {@link Node}.
		 *
		 * @param position
		 *            the {@link Location} presenting node
		 */
		public Node(Location position) {
			this(position.x, position.y);
		}

		/**
		 * Constructor of {@link Node}.
		 *
		 * @param x
		 *            x-axis of node
		 * @param y
		 *            y-axis of node
		 */
		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		/**
		 * Transfer to {@link Location}.
		 *
		 * @return an instance of {@link Location} represents the node
		 */
		public Location toLocation() {
			return new Location(x, y);
		}

		/**
		 * Obtain the surrounding nodes.
		 *
		 * @return a list of surrounding {@link Node}
		 */
		public List<Node> getSurrounds() {
			List<Node> surrounds = new LinkedList<>();
			surrounds.add(new Node(x, y + 1)); // N
			surrounds.add(new Node(x, y - 1)); // S
			surrounds.add(new Node(x - 1, y)); // W
			surrounds.add(new Node(x + 1, y)); // E
			return surrounds;
		}

		@Override
		public int compareTo(Node other) {
			return f - other.f;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
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
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		private AStar getOuterType() {
			return AStar.this;
		}

	}

	/* test method
	public static void main(String[] args) {
		GridWorldModel model = new ArenaModel();
		List<Location> result = new AStar(model).findPath(new Location(1, 1), new Location(3, 4));
		System.out.println("Steps: " + result.size());
		for (Location cell : result) {
			System.out.println("[" + cell + "]");
		}
	}
	 */

}
