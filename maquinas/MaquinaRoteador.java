package maquinas;

import java.util.SortedMap;

import implementacoes_udp.roteador.UDPdeRoteador;
import modelos.EnderecoDeMaquina;

public class MaquinaRoteador 
{

    private final EnderecoDeMaquina servidor;
    private final EnderecoDeMaquina roteador;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;
    private final SortedMap<Integer,Integer> atrasosDePropagacao;
    private final SortedMap<Integer,Integer> atrasosDeTransmissao;
    private final SortedMap<Integer,Double> probabilidadesDePerda;

    private int tamanhoDoPacote;
    private int tamanhoDaFilaDePacotes;

    public MaquinaRoteador (
        EnderecoDeMaquina servidor,
        EnderecoDeMaquina roteador,
        SortedMap<Integer,EnderecoDeMaquina> clientes,
        SortedMap<Integer,Integer> atrasosDePropagacao,
        SortedMap<Integer,Integer> atrasosDeTransmissao, 
        SortedMap<Integer,Double> probabilidadesDePerda
    )
    {
        this.servidor = servidor;
        this.roteador = roteador;
        this.clientes = clientes;
        this.atrasosDePropagacao = atrasosDePropagacao;
        this.atrasosDeTransmissao = atrasosDeTransmissao;
        this.probabilidadesDePerda = probabilidadesDePerda;

        this.tamanhoDoPacote = -1;
        this.tamanhoDaFilaDePacotes = -1;
    }

    public void setTamanhoDoPacote ( int tamanhoDoPacote )
    {
        this.tamanhoDoPacote = tamanhoDoPacote;
    }

    public void setTamanhoDaFilaDePacotes ( int tamanhoDeJanela )
    {
        this.tamanhoDaFilaDePacotes = tamanhoDeJanela;
    }
    
    public void run ()
        throws Exception
    {

        UDPdeRoteador udpDoRoteador = 
            new UDPdeRoteador(
                roteador.getNome(),
                roteador.getPorta(),
                servidor,
                clientes,
                atrasosDePropagacao,
                atrasosDeTransmissao,
                probabilidadesDePerda
            );

        if ( this.tamanhoDoPacote > -1 )
        {
            udpDoRoteador.setTamanhoDoPacote( this.tamanhoDoPacote );
        }

        if ( this.tamanhoDaFilaDePacotes > -1 )
        {
            udpDoRoteador.setTamanhoDaFilaDePacotes( this.tamanhoDaFilaDePacotes );
        }

        udpDoRoteador.start();

    }
    
}
