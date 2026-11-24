package me.rotxiv.analex.application;

public class Analex {   //Es una cinta de tokens
    private static final int ACEPTAR_TOKEN = -1;
    private static final int ESTADO_CERO = 0;
    private static final int ESTADO_ID = 1;
    private static final int ESTADO_NUM = 2;
    private static final int ESTADO_LITERAL = 3;
    private static final int ESTADO_SLASH = 4;
    private static final int ESTADO_SIMBOLO = 5;

    private static final String[] TPC = {
        "PROGRAM", "VAR", "PROCEDURE", "VOID", "BEGIN", "END",
        "IF", "THEN", "ELSE", "FOR", "TO", "DO", "DOWNTO", "WHILE",
        "CASE", "OF", "REPEAT", "UNTIL", "READ", "READLN", "PRINT",
        "PRINTLN", "NOT", "AND", "OR", "MOD", "DIV", "BOOLEAN", "INTEGER"
    };
    private static final int[] TPC_TOK = {
        Token.PROGRAM, Token.VAR, Token.PROCEDURE, Token.PROCEDURE, Token.BEGIN, Token.END,
        Token.IF, Token.THEN, Token.ELSE, Token.FOR, Token.TO, Token.DO, Token.DOWNTO, Token.WHILE,
        Token.CASE, Token.OF, Token.REPEAT, Token.UNTIL, Token.READ, Token.READLN, Token.PRINT,
        Token.PRINTLN, Token.NOT, Token.AND, Token.OR, Token.MOD, Token.DIV, Token.TIPO, Token.TIPO
    };

    private Cinta M;
    private Token R;        //Result del dt (Preanalisis)
    private String ac;
    private int pos;        //Posición de inicio del lexema del preanalisis(), calculado en el dt(). 
                            //Use Cinta.getPos() o sea pos=M.getPos();
    
    public Analex(Cinta c){
        M = c;
        R = new Token();
        init();
    }
    
    public final void init(){
        M.init();
        avanzar();      //Calcular el primer preanalisis.
    }
    
    public Token Preanalisis(){
        return R;
    }
    
    public String lexema(){
        return ac;
    }
    
    public void avanzar(){
       dt();
       resaltar();
    }
    
    private void dt(){
        int estado = ESTADO_CERO;
        ac = "";
        while (estado != ACEPTAR_TOKEN) {
            char cc = M.cc();
            switch (estado) {
                case ESTADO_CERO:
                    estado = caseCero(cc);
                    break;
                case ESTADO_ID:
                    estado = caseID();
                    break;
                case ESTADO_NUM:
                    estado = caseNUM();
                    break;
                case ESTADO_LITERAL:
                    estado = caseLiteral();
                    break;
                case ESTADO_SLASH:
                    estado = caseSlash(cc);
                    break;
                case ESTADO_SIMBOLO:
                    estado = caseSimbolo();
                    break;
                default:
                    R.set(Token.ERROR, 0);
                    estado = ACEPTAR_TOKEN;
                    break;
            }
        }
    }

    private int caseCero(char cc) {
        if (esEspacio(cc)) {
            M.avanzar();
            return ESTADO_CERO;
        }

        pos = M.getPos();
        if (cc == Cinta.EOF) {
            R.set(Token.FIN, 0);
            return ACEPTAR_TOKEN;
        }
        if (esLetra(cc)) {
            ac += Character.toUpperCase(cc);
            M.avanzar();
            return ESTADO_ID;
        }
        if (esDigito(cc)) {
            ac += cc;
            M.avanzar();
            return ESTADO_NUM;
        }
        if (cc == '\'' || cc == '"') {
            ac += cc;
            M.avanzar();
            return ESTADO_LITERAL;
        }
        if (cc == '/') {
            ac = "/";
            M.avanzar();
            return ESTADO_SLASH;
        }

        ac = String.valueOf(cc);
        M.avanzar();
        return ESTADO_SIMBOLO;
    }

    private int caseID() {
        StringBuilder palabra = new StringBuilder(ac);
        while (esLetra(M.cc()) || esDigito(M.cc())) {
            palabra.append(Character.toUpperCase(M.cc()));
            M.avanzar();
        }
        ac = palabra.toString();

        int token = buscarTPC(ac);
        if (token == Token.TIPO) {
            R.set(Token.TIPO, "BOOLEAN".equals(ac) ? Token.BOOLEAN : Token.INTEGER);
        } else if (token != -1) {
            R.set(token, 0);
        } else {
            R.set(Token.ID, -1);
        }
        return ACEPTAR_TOKEN;
    }

    private int caseNUM() {
        StringBuilder numero = new StringBuilder(ac);
        while (esDigito(M.cc())) {
            numero.append(M.cc());
            M.avanzar();
        }
        ac = numero.toString();
        try {
            R.set(Token.NUM, Integer.parseInt(ac));
        } catch (NumberFormatException ex) {
            R.set(Token.ERROR, 0);
        }
        return ACEPTAR_TOKEN;
    }

    private int caseLiteral() {
        char comilla = ac.charAt(0);
        StringBuilder literal = new StringBuilder(ac);
        while (M.cc() != comilla && M.cc() != Cinta.EOLN && M.cc() != Cinta.EOF) {
            literal.append(M.cc());
            M.avanzar();
        }
        if (M.cc() == comilla) {
            literal.append(M.cc());
            M.avanzar();
            R.set(Token.LITERAL, 0);
        } else {
            R.set(Token.ERROR, 0);
        }
        ac = literal.toString();
        return ACEPTAR_TOKEN;
    }

    private int caseSlash(char cc) {
        if (cc == '/') {
            while (M.cc() != Cinta.EOF && M.cc() != Cinta.EOLN) {
                M.avanzar();
            }
            ac = "";
            return ESTADO_CERO;
        }
        if (cc == '*') {
            return caseBlockComment();
        }
        R.set(Token.DIV, 0);
        return ACEPTAR_TOKEN;
    }

    private int caseBlockComment() {
        M.avanzar();
        StringBuilder comentario = new StringBuilder("/*");
        while (M.cc() != Cinta.EOF) {
            char actual = M.cc();
            comentario.append(actual);
            M.avanzar();
            if (actual == '*' && M.cc() == '/') {
                comentario.append('/');
                M.avanzar();
                ac = "";
                return ESTADO_CERO;
            }
        }
        ac = comentario.toString();
        R.set(Token.ERROR, 0);
        return ACEPTAR_TOKEN;
    }

    private int caseSimbolo() {
        char cc = ac.charAt(0);
        switch (cc) {
            case ',':
                R.set(Token.COMA, 0);
                break;
            case ':':
                if (M.cc() == '=') {
                    ac += M.cc();
                    M.avanzar();
                    R.set(Token.ASSIGN, 0);
                } else {
                    R.set(Token.DOSPTOS, 0);
                }
                break;
            case ';':
                R.set(Token.PTOCOMA, 0);
                break;
            case '(':
                R.set(Token.PA, 0);
                break;
            case ')':
                R.set(Token.PC, 0);
                break;
            case '{':
                R.set(Token.BEGIN, 0);
                break;
            case '}':
                R.set(Token.END, 0);
                break;
            case '!':
                if (M.cc() == '=') {
                    ac += M.cc();
                    M.avanzar();
                    R.set(Token.OPREL, Token.DIS);
                } else {
                    R.set(Token.NOT, 0);
                }
                break;
            case '&':
                R.set(Token.AND, 0);
                break;
            case '|':
                R.set(Token.OR, 0);
                break;
            case '+':
                R.set(Token.MAS, 0);
                break;
            case '-':
                R.set(Token.MENOS, 0);
                break;
            case '*':
                R.set(Token.POR, 0);
                break;
            case '%':
                R.set(Token.MOD, 0);
                break;
            case '=':
                R.set(Token.OPREL, Token.IGUAL);
                break;
            case '<':
                if (M.cc() == '=') {
                    ac += M.cc();
                    M.avanzar();
                    R.set(Token.OPREL, Token.MEI);
                } else if (M.cc() == '>') {
                    ac += M.cc();
                    M.avanzar();
                    R.set(Token.OPREL, Token.DIS);
                } else {
                    R.set(Token.OPREL, Token.MEN);
                }
                break;
            case '>':
                if (M.cc() == '=') {
                    ac += M.cc();
                    M.avanzar();
                    R.set(Token.OPREL, Token.MAI);
                } else {
                    R.set(Token.OPREL, Token.MAY);
                }
                break;
            default:
                R.set(Token.ERROR, 0);
                break;
        }
        return ACEPTAR_TOKEN;
    }
    
    private boolean esEspacio(char cc) {
        return cc == ' ' || cc == '\t' || cc == Cinta.EOLN;
    }

    private boolean esDigito(char cc) {
        return cc >= '0' && cc <= '9';
    }

    private boolean esLetra(char cc) {
        cc = Character.toUpperCase(cc);
        return cc >= 'A' && cc <= 'Z';
    }

    private int buscarTPC(String palabra) {
        for (int i = 0; i < TPC.length; i++) {
            if (TPC[i].equals(palabra)) {
                return TPC_TOK[i];
            }
        }
        return -1;
    }
    
    public void resaltar(){    //Para resaltar el lexema en el progFuente
        comunicate(pos, lexema());
    }

    /**
     * Método extensible por la Interfaz gráfica para recibir la posición y el
     * lexema y poder resaltarlo en la vista. Por defecto no hace nada.
     * Subclasses o instancias anónimas pueden sobrescribirlo.
     *
     * @param pos   posición inicial del lexema en la cinta
     * @param lexema el texto del lexema
     */
    public void comunicate(int pos, String lexema) {
        // No hace nada por defecto. La GUI puede sobrescribir este método.
    }
}
