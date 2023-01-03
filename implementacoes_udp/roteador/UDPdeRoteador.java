package implementacoes_udp.roteador;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import modelos.EnderecoDeMaquina;

public class UDPdeRoteador 
    extends Thread
{

    private DatagramSocket socket;

    private final EnderecoDeMaquina roteador;
    private final EnderecoDeMaquina servidor;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;

    private int tamanhoDoPacote;
    private int tamanhoDaFilaDePacotes;

    private final SortedMap<Integer,Integer> atrasosDePropagacao;
    private final SortedMap<Integer,Integer> atrasosDeTransmissao;
    private final SortedMap<Integer,Double> probabilidadesDePerda;

    private LinkedList<DatagramPacket> bufferDePacotes;

    private ThreadDeEntrada threadDeEntrada;
    private ThreadDeSaida threadDeSaida;

    private long inicioDeFuncionamento;
    private File outputDaFilaDeRoteador;

    /**
     * 
     *      CONSTRUTOR
     * 
     */

    public UDPdeRoteador (
        String denominacao,
        int portaDoRoteador,
        EnderecoDeMaquina servidor,
        SortedMap<Integer,EnderecoDeMaquina> clientes,
        SortedMap<Integer,Integer> atrasosDePropagacao,
        SortedMap<Integer,Integer> atrasosDeTransmissao,
        SortedMap<Integer,Double> probabilidadesDePerda
    )
        throws Exception
    {

        this.roteador = new EnderecoDeMaquina(
            denominacao, 
            InetAddress.getLocalHost(), 
            portaDoRoteador
        );

        this.tamanhoDoPacote = 1000;
        this.tamanhoDaFilaDePacotes = 10;

        this.servidor = servidor;
        this.clientes = clientes;

        this.atrasosDePropagacao = atrasosDePropagacao;
        this.atrasosDeTransmissao = atrasosDeTransmissao;
        this.probabilidadesDePerda = probabilidadesDePerda;

        this.bufferDePacotes = new LinkedList<DatagramPacket>();

    }

    /**
     * 
     *      SETTERS
     * 
     */

    public void setTamanhoDoPacote ( int tamanhoDoPacote )
    {
        this.tamanhoDoPacote = tamanhoDoPacote;
    }

    public void setTamanhoDaFilaDePacotes ( int tamanhoDeJanela )
    {
        this.tamanhoDaFilaDePacotes = tamanhoDeJanela;
    }

    /**
     * 
     *      GETTERS
     * 
     */

    String getDenominacao () 
    {
        return this.roteador.getNome();
    }

    DatagramSocket getSocket ()
    {
        return this.socket;
    }

    InetAddress getEnderecoIPdoRoteador () 
    {
        return this.roteador.getEnderecoIP();
    }

    String getNomeDoServidor() 
    {
        return this.servidor.getNome();
    }

    InetAddress getEnderecoIPdoServidor () 
    {
        return this.servidor.getEnderecoIP();
    }

    int getPortaDoServidor () 
    {
        return this.servidor.getPorta();
    }

    SortedMap<Integer,EnderecoDeMaquina> getClientes () 
    {
        return this.clientes;
    }

    int getTamanhoDoPacote () 
    {
        return this.tamanhoDoPacote;
    }
    
    /**
     * 
     *      MÉTODOS IMPLEMENTADOS
     * 
     */

    void inicializarOutputFilaDoRoteador ()
        throws Exception
    {

        for ( int i = 1; i < 100; i++ ) {

            String path = 
                "resultados" 
                + File.separator 
                + "filas_de_roteador" 
                + File.separator 
                + this.getDenominacao()
                + "_"
                + i
                + ".txt";
            
            File f = new File( path );
            if( ! f.exists() && ! f.isDirectory() ) { 

                f.getParentFile().mkdirs();
                f.createNewFile();

                this.outputDaFilaDeRoteador = f;
        
                BufferedWriter writer = 
                new BufferedWriter( 
                    new FileWriter(
                        new File ( path )
                    ) 
                );
                writer.write( "Tempo (s),Tamanho da Fila\n" );
                writer.write( "0,0\n" );
                writer.close();

                break;
            }
            
        }
        
    }

    void registrarOutputFilaDoRoteador ()
    {

        double tempoAtual = ( System.currentTimeMillis() - udp.getInicioDeTransmissao() ) / 1000 ;

        FileWriter fw = new FileWriter( 
            this.outputDaFilaDeRoteador.getAbsolutePath(), 
            true
        );
        BufferedWriter bw = new BufferedWriter( fw );
        bw.write(
            tempoAtual 
            + ","
            + this.bufferDePacotes.size()
        );
        bw.newLine();
        bw.close();

    }

    boolean existePacotesNoBuffer() 
    {
        return ( this.bufferDePacotes.size() > 0 );
    }

    void adicionarPacoteAoBuffer( DatagramPacket pacote ) 
    {
        if ( this.bufferDePacotes.size() < this.tamanhoDaFilaDePacotes )
        {
            this.bufferDePacotes.add( pacote );
        }
    }

    DatagramPacket removerPacoteDoBuffer ()
        throws Exception
    {
        if ( this.bufferDePacotes.size() > 0 )
        {
            try 
            {
                return this.bufferDePacotes.removeFirst();
            } 
            catch ( NoSuchElementException e ) 
            {
                // SKIP BUFFER READ
            }
        }
        return null;
    }

    void enviePacoteAoCliente ( 
        int idDoCliente,
        byte[] pacoteParaCliente 
    )
        throws Exception
    {

        EnderecoDeMaquina cliente = this.clientes.get( idDoCliente );
                        
        DatagramPacket pacoteDeEnvio =
            new DatagramPacket(
                pacoteParaCliente, 
                pacoteParaCliente.length,
                cliente.getEnderecoIP(),
                cliente.getPorta()
            );

        enviarPacote( idDoCliente, pacoteDeEnvio, pacoteParaCliente.length );

    }

    void enviePacoteAoServidor( byte[] pacoteParaServidor )
        throws Exception
    {
                        
        DatagramPacket pacoteDeEnvio =
            new DatagramPacket(
                pacoteParaServidor, 
                pacoteParaServidor.length,
                this.servidor.getEnderecoIP(),
                this.servidor.getPorta()
            );

        enviarPacote( 0, pacoteDeEnvio, pacoteParaServidor.length );

    }

    void enviarPacote (
        int idMaquina,
        DatagramPacket pacoteDeEnvio, 
        int length 
    )
        throws Exception
    {

        sleep( this.atrasosDeTransmissao.get( idMaquina ) );

        if( 
            Math.random() < ( 1 - this.probabilidadesDePerda.get( idMaquina ) )
        )
        {
            this.socket.send( pacoteDeEnvio );
        }

    }

    public void run () 
    {

        try {

            this.socket = new DatagramSocket( this.roteador.getPorta() );
                
            this.threadDeSaida = new ThreadDeSaida( this );
            this.threadDeEntrada = new ThreadDeEntrada( this, this.atrasosDePropagacao );
    
            this.threadDeSaida.start();
            this.threadDeEntrada.start();

            this.inicioDeFuncionamento = System.currentTimeMillis();
            inicializarOutputFilaDoRoteador();

            System.out.println( this.roteador.getNome() + ": Em funcionamento..." );

        } 
        catch ( Exception e ) 
        {
            
            e.printStackTrace();
            System.exit( -1 );
            
        }
    
    }
    
}
