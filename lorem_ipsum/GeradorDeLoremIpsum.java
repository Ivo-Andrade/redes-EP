package lorem_ipsum;

public class GeradorDeLoremIpsum {

    // 1024 caracteres de lorem ipsum
    private static String loremIpsum = 
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
        "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
        "Potenti nullam ac tortor vitae purus faucibus ornare suspendisse sed. " +
        "Nullam vehicula ipsum a arcu cursus. " + 
        "Eget arcu dictum varius duis at consectetur lorem. " +
        "Ut aliquam purus sit amet luctus venenatis lectus magna fringilla. " +
        "Vestibulum lorem sed risus ultricies. " +
        "Scelerisque felis imperdiet proin fermentum leo vel. " +
        "Aliquam etiam erat velit scelerisque in dictum non. " +
        "Pharetra et ultrices neque ornare aenean euismod elementum. " +
        "Gravida in fermentum et sollicitudin ac. " +
        "Habitant morbi tristique senectus et netus. " +
        "Ullamcorper dignissim cras tincidunt lobortis feugiat vivamus at augue eget. " +
        "Nulla pharetra diam sit amet nisl suscipit adipiscing. " +
        "Tincidunt tortor aliquam nulla facilisi cras fermentum odio eu. " +
        "Dolor purus non enim praesent elementum facilisis leo vel. " +
        "Pretium quam vulputate dignissim suspendisse. Et leo duis ut diam quam. " +
        "Tellus molestie nunc non blandit massa enim nec. Leo purus. ";

    
    public static String gerarLoremIpsum ( int characteres, int offset ) 
    {

        StringBuilder stringBuilder = new StringBuilder();
        
        int charsRequeridos = characteres + offset;
        while ( charsRequeridos > 0 ) 
        {

            if ( charsRequeridos >= 1024 )
            {

                stringBuilder.append( loremIpsum );
                charsRequeridos -= 1024;

            }
            else if ( charsRequeridos > 0 ) 
            {

                stringBuilder.append( 
                    loremIpsum.substring( 0, charsRequeridos )
                );

                charsRequeridos -= charsRequeridos;

            }
            
        }

        return stringBuilder.substring( offset, stringBuilder.toString().length() );

    }

    public static String gerarLoremIpsum ( int characteres ) 
    {

        return gerarLoremIpsum( characteres, 0 );

    }
    
}
