import java.util.ArrayList;

@Service
public class GameService {
    private boolean whiteToMove;

    private String id_white;
    private String id_black;

    private PlayerTimer whiteTimer;
    private PlayerTimer blackTimer;

    private int timer;
    private int increment;

    private Board board;
    private ArrayList<Move> currentPlayerMoves;

    public GameService(String idw, String idb, int t, int i) {
        id_white = idw;
        id_black = idb;

        timer = t;
        increment = i;

        whiteTimer = new PlayerTimer(t, i, this::onWhiteTimeout);
        whiteTimer = new PlayerTimer(t, i, this::onBlackTimeout);

        whiteToMove = true;
        board = new Board();
        currentPlayerMoves = new ArrayList<>();
    }

    public void Init() { Init(true); }

    public void Init(boolean official) {
        board.Init();

        if (official) {
            board.LoadStartPos();
        }
    }

    public void StartGame() {
        whiteTimer.start();
    }

    public void switchTurn() {
        if (whiteTomove) {
            whiteTimer.stop();
            blackTimer.start();
        }
        else {
            blackTimer.stop();
            whiteTimer.start();
        }

        whiteToMove = !whiteToMove;
    }

    private void onWhiteTimeout() {
        System.out.println("");
    }


    private void onBlackTimeout() {
        System.out.println("");
    }
}
