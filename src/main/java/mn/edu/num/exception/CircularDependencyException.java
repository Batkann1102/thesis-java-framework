package mn.edu.num.exception;

/**
 * Хоёр болон түүнээс дээш Bean-үүд бие биеэсээ хамаарч тойрог (circular) үүсгэсэн тохиолдолд
 * хамаарлын сүлжээг тасалж энэхүү Exception-ийг шиднэ.
 * Энэ нь хязгааргүй хамаарал үүсгэн stack overflow болохоос сэргийлдэг.
 */
public class CircularDependencyException extends RuntimeException{
    public CircularDependencyException(String message){
        super(message);
    }
}