import java.util.HashMap;
import java.util.Map.Entry;

public class Mouse {
	//* Private Constants
		// Game Attributes
	private static final double NEED_DEATH = 100.0;
		// Mouse Attributes
	private static final double AGERATE = 0.01;
	private static final double MAX_FATIGUERATE = 0.54;
	private static final double MIN_FATIGUERATE = 0.01;
	private static final double MAX_HUNGERRATE = 0.44;
	private static final double MIN_HUNGERRATE = 0.045;
	private static final double MAX_LIFESPAN = 8.9;
	private static final double MIN_LIFESPAN = 3.5;
	private static final double RESTRATE = 1.55; //redo: make a physical attribute with low variability
	private static final int SKIP_EAT = 7;
	private static final int SKIP_REST = 35;

	//*Private Fields
		// Game Attributes
	private final double LIFESPAN;
	private int skipCycles;
		// Mouse Attributes
	private final double HUNGERRATE;
	private final double FATIGUERATE;
	private double hunger, fatigue;
	//thirst, discomfort, energy, sex, warmth, etc etc TODO "I have a lot of...______"
	private double age; // how long it has been living
	//health, sex(M/F), fit to breed, ispregnant, etc etc TODO
	//TODO private HashMap<String, Boolean> statusAffects = new HashMap<>(); //weak, diseased, lactose intolerant, ispregnant? etc etc TODO //sleepy (adjust fatigue limit)
	private final int birthday;
	private Gender gender;
	private boolean isAlive;
	private final String name;
	private Position position;
	private int walkRate;

	private AI brain;
	
	public Mouse(String name, Position p, Mouse mother, Mouse father) {

		this.birthday = MouseSim.getRuntime();
		this.gender = (MouseSim.rand.nextInt(2) == 1) ? Gender.MALE : Gender.FEMALE;
		this.isAlive = true;
		this.name = name; //redo: generate name and remove from param list (pass in father lastname)
		this.position = p;
		this.walkRate = 1;

		skipCycles = 0;

		this.hunger = 0.0;
		this.fatigue = 0.0;
		this.age = 0.0;

		this.brain = new AI(this);

		// Create life-rates
		LIFESPAN = MIN_LIFESPAN + (MAX_LIFESPAN - MIN_LIFESPAN) * MouseSim.rand.nextDouble();
		HUNGERRATE = MIN_HUNGERRATE + (MAX_HUNGERRATE - MIN_HUNGERRATE) * MouseSim.rand.nextDouble();
		FATIGUERATE = MIN_FATIGUERATE + (MAX_FATIGUERATE - MIN_FATIGUERATE) * MouseSim.rand.nextDouble();
		
		Stream.update(name + " (" + gender + ") " + " was born!");
		MouseSim.getWorld().getWorldNode(this.position).add((Mouse)this);
	}

	private void adjustHunger(double amt) {
		if(!isAlive) return;

		hunger += amt;

		if(hunger < 0.0) {
			hunger = 0.0;
		}

		if(hunger > NEED_DEATH) {
			this.die("starved to death");
		}
	}

	private void adjustFatigue(double amt) {
		if(!isAlive) return;

		fatigue += amt;

		if(fatigue < 0.0) {
			fatigue = 0.0;
		}

		if(fatigue > NEED_DEATH) {
			this.die("died of exhaustion");
		}
	}

	private boolean canMove(Direction d, int steps) {
		if(!isAlive) return false;

		int size = MouseSim.getWorldSize();

		switch(d) {
			case UP:
				return this.position.row - steps >= 0 ;
			case DOWN:
				return this.position.row + steps <= size-1;
			case LEFT:
				return this.position.col - steps >= 0;
			case RIGHT:
				return this.position.col + steps <= size-1;
			case UPLEFT:
				return this.position.row - steps >= 0 && this.position.col - steps >= 0;
			case UPRIGHT:
				return this.position.row - steps >= 0 && this.position.col + steps <= size-1;
			case DOWNLEFT:
				return this.position.row + steps <= size-1 && this.position.col - steps >= 0;
			case DOWNRIGHT:
				return this.position.row + steps <= size-1 && this.position.col + steps <= size-1;
			default:
				return false;
		}
	}



	private void die(String reason) { //REDO
		String message = name + " has " + reason + "! RIP (" + birthday + "-" + MouseSim.getRuntime() + ")";
		Stream.update(message);

		//Kill if not reincarnated
		if(!this.reincarnation()) this.isAlive = false;;
	}

	private void eat(Food food) {
		if(!isAlive) return;

		adjustHunger(-food.eat(hunger));
	}

	public String getName() {
		return name;
	}

	public Position getPosition() {
		return position;
	}

	public boolean isAlive() {
		return isAlive;
	}

	private void move(Direction d, int steps) {
		if(!isAlive) return;

		MouseSim.getWorld().getWorldNode(this.position).remove(this);

		switch (d){
			case UP:
				position.row -= steps;
			break;
			case DOWN:
				position.row += steps;
			break;
			case LEFT:
				position.col -= steps;
			break;
			case RIGHT:
				position.col += steps;
			break;
			case UPLEFT:
				position.row -= steps;
				position.col -= steps;
			break;
			case UPRIGHT:
				position.row -= steps;
				position.col += steps;
			break;
			case DOWNLEFT:
				position.row += steps;
				position.col -= steps;
			break;
			case DOWNRIGHT:
				position.row += steps;
				position.col += steps;
			break;
			default:
		}

		MouseSim.getWorld().getWorldNode(this.position).add(this);

		adjustFatigue(FATIGUERATE);
	}

	private void moveRandom(int steps) {
		if(!isAlive) return;

		switch(MouseSim.rand.nextInt(8)){
			case 0:
				if(canMove(Direction.UP, steps)) {
					move(Direction.UP, steps);
				}
			break;
			case 1:
				if(canMove(Direction.DOWN, steps)) {
					move(Direction.DOWN, steps);
				}
			break;
			case 2:
				if(canMove(Direction.LEFT, steps)) {
					move(Direction.LEFT, steps);
				}
			break;
			case 3:
				if(canMove(Direction.RIGHT, steps)) {
					move(Direction.RIGHT, steps);
				}
			break;
			case 4:
				if(canMove(Direction.UPLEFT, steps)) {
					move(Direction.UPLEFT, steps);
				}
			break;
			case 5:
				if(canMove(Direction.UPRIGHT, steps)) {
					move(Direction.UPRIGHT, steps);
				}
			break;
			case 6:
				if(canMove(Direction.DOWNLEFT, steps)) {
					move(Direction.DOWNLEFT, steps);
				}
			break;
			case 7:
				if(canMove(Direction.DOWNRIGHT, steps)) {
					move(Direction.DOWNRIGHT, steps);
				}
			break;
			default:
		}

	}

	private void printStats() {
		if(!isAlive) return;

		System.out.println("Name:   \t" + name);
		System.out.println("Hunger: \t" + hunger);
		System.out.println("Fatigue:\t" + fatigue);
		System.out.println("Age:    \t" + age);
		System.out.println(LIFESPAN + "  " + HUNGERRATE + "  " + FATIGUERATE); //debug

		////TODO
		// for(Entry<String, Boolean> entry : statusAffects.entrySet()) {
		// 	String affect = entry.getKey();
		// 	Boolean hasAffect = entry.getValue();
		// 	if(hasAffect) {
		// 		System.out.println("You are " + affect);
		// 	}
		// }
		////TODO

		System.out.println();
	}

	private boolean reincarnation() { //REDO
		if(MouseSim.rand.nextInt(5) == 0) {
			this.hunger = this.hunger / 2;
			this.fatigue = this.fatigue / 3;
			this.age = 0.0;
			Stream.update(name + " was reincarnated! Amazing!");
			return true;
		}

		return false;
	}

	public void update() {
		if(!isAlive) return;
		
		updateAge(); 
		printStats();

		if(skipCycles != 0) {
			skipCycles--;
			return;
		}

		switch(brain.chooseAction()) {
			case MOVE:
				moveRandom(this.walkRate); 
				adjustHunger(HUNGERRATE);
			break;

			case EAT:
				Stream.update(name+" decided to eat!");
				this.eat(MouseSim.getWorld().getWorldNode(this.position).getAnyFood());
				skipCycles = SKIP_EAT;
			break;

			case REST: // redo: make a smart decision about how long to rest? //wake up if another need gets critical?
				Stream.update(name+" decided to take a little snooze... zZzz...");
				adjustFatigue(-RESTRATE * SKIP_REST);
				skipCycles = SKIP_REST;
				adjustHunger(HUNGERRATE * (SKIP_REST/10));
			break;

			default:
		}

	}

	private void updateAge() {
		if(!isAlive) return;

		age += AGERATE;

		if(age > LIFESPAN) {
			this.die("died of old age");
		}
	}

	/// redo: MENTAL ATTRIBUTES = BRAIN, PHYSICAL ATTRIBUTES = BODY, BOTH PASSED TO CHILD
	private class AI { 
		//* Private Constants
		private static final double HUNGER_LIMIT = 50.0; //Redo: set on a per-mouse basis -- pass to child
		private static final double FATIGUE_LIMIT = 70.0; //Redo: set on a per-mouse basis -- pass to child

		private Mouse body;

		private AI(Mouse body) {
			this.body = body;
		}

		private MouseAction chooseAction() {
			WorldNode currentLocation = MouseSim.getWorld().getWorldNode(body.position);

			if(currentLocation.hasFood() && hunger > HUNGER_LIMIT) {
				return MouseAction.EAT;
			}

			if(fatigue > FATIGUE_LIMIT) {
				return MouseAction.REST;
			}

			return MouseAction.MOVE;
		}
	}

}