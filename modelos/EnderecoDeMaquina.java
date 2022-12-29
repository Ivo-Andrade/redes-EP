package modelos;

import java.net.InetAddress;

public class EnderecoDeMaquina {

    private final String nome;
    private final InetAddress enderecoIP;
    private final int porta;

    public EnderecoDeMaquina (
        String nome,
        InetAddress enderecoIP,
        int porta
    )
    {
        this.nome = nome;
        this.enderecoIP = enderecoIP;
        this.porta = porta;
    }

    public String getNome ()
    {
        return this.nome;
    }

    public InetAddress getEnderecoIP ()
    {
        return this.enderecoIP;
    }

    public int getPorta ()
    {
        return this.porta;
    }
    
}
