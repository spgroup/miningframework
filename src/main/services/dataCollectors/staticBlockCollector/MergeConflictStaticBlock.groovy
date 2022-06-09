package services.dataCollectors.staticBlockCollector

import org.apache.commons.io.FileUtils
@Grab(group = 'commons-io', module = 'commons-io', version = '2.6')
import org.apache.commons.lang3.StringUtils

import java.nio.charset.Charset
import java.nio.file.Path

class MergeConflictStaticBlock {

    public static MINE_CONFLICT_MARKER = "<<<<<<<MINE"
	public static CHANGE_CONFLICT_MARKER = "======="
	public static YOURS_CONFLICT_MARKER = ">>>>>>>YOURS"
    public static TOKEN_PARSER_TERMINATOR = ";"
    public static TOKEN_PARSER_KEYS = "}"
    public static TOKEN_STATIC_NONE = "{"
	public static TOKEN_CLASS_NONE = "class"
    public static TOKEN_STATIC_BLOCK = "static{"
	
    /**
     * @param file
     * @return the set of merge conflicts for static blocks present in the given file
     */
    static int extractMergeConflictsForStaticBlock(Path file) {
        int count = 0;
		String linePrevious = null,linePreviousOfPrevious = null;
		boolean hasBlock = false, isOtherBlock = false , isGitErro = false;
        Iterator<String> mergeCodeLines = FileUtils.readLines(file.toFile(), Charset.defaultCharset()).iterator()
        while (mergeCodeLines.hasNext()) {
            String line = mergeCodeLines.next()
		  if(!line.isEmpty()) { 
     		if(line!=null && linePrevious == null && linePreviousOfPrevious == null) {
	            	 linePrevious = line;
	        }else if(line!=null && linePrevious != null && linePreviousOfPrevious == null) {
	            	 linePreviousOfPrevious=linePrevious;
	            	 linePrevious=line;
	        }else {

				     boolean a = StringUtils.deleteWhitespace(linePreviousOfPrevious).contains(TOKEN_PARSER_TERMINATOR)
				     boolean b = StringUtils.deleteWhitespace(linePreviousOfPrevious).contains(TOKEN_PARSER_KEYS)
					 boolean c = StringUtils.deleteWhitespace(linePrevious).contains(MINE_CONFLICT_MARKER)
				     boolean f = StringUtils.deleteWhitespace(linePrevious).contains(CHANGE_CONFLICT_MARKER)
				     boolean d = StringUtils.deleteWhitespace(line).equals(TOKEN_STATIC_BLOCK)
					 boolean e = StringUtils.deleteWhitespace(line).equals(TOKEN_STATIC_NONE)

				     //match outside the block
				if((StringUtils.deleteWhitespace(linePreviousOfPrevious).contains(TOKEN_PARSER_TERMINATOR) ||StringUtils.deleteWhitespace(linePreviousOfPrevious).contains(TOKEN_CLASS_NONE) || StringUtils.deleteWhitespace(linePreviousOfPrevious).contains(TOKEN_PARSER_KEYS) || StringUtils.deleteWhitespace(linePreviousOfPrevious).contains(MINE_CONFLICT_MARKER)) &&
						(StringUtils.deleteWhitespace(linePrevious).startsWith(MINE_CONFLICT_MARKER) || StringUtils.deleteWhitespace(linePrevious).contains(CHANGE_CONFLICT_MARKER)) &&
						(StringUtils.deleteWhitespace(line).contains(TOKEN_STATIC_BLOCK) || StringUtils.deleteWhitespace(line).startsWith(TOKEN_STATIC_NONE))){
					count++;
				}

				     // match inside the block
				if(StringUtils.deleteWhitespace(linePrevious).startsWith(TOKEN_STATIC_BLOCK) || StringUtils.deleteWhitespace(linePrevious).startsWith(TOKEN_STATIC_NONE)) {
					hasBlock = true;
				}
				if(hasBlock && StringUtils.deleteWhitespace(line).contains(TOKEN_STATIC_NONE)) {
					isOtherBlock = true;
				}
				if(hasBlock && isOtherBlock && StringUtils.deleteWhitespace(line).contains(TOKEN_PARSER_KEYS) && checkLineWithTwoTokenTerminal(line)) {
					isOtherBlock = false;
				}
				if(hasBlock && (StringUtils.deleteWhitespace(line).startsWith(MINE_CONFLICT_MARKER) || StringUtils.deleteWhitespace(line).startsWith(CHANGE_CONFLICT_MARKER) || StringUtils.deleteWhitespace(line).startsWith(YOURS_CONFLICT_MARKER))){
					isGitErro = true;
				}
				if(hasBlock && !isOtherBlock && StringUtils.deleteWhitespace(line).contains(TOKEN_PARSER_KEYS) &&  isGitErro) {
					hasBlock = false;
					isGitErro = false;
					count++;
				}else if(hasBlock && !isOtherBlock && StringUtils.deleteWhitespace(line).contains(TOKEN_PARSER_KEYS)) {
					hasBlock = false;
				}
	            	 linePreviousOfPrevious = linePrevious;
	            	 linePrevious = line;
	             }
		  }
		}
        return count
    }
	private static boolean checkLineWithTwoTokenTerminal(String line) {
		if(StringUtils.deleteWhitespace(line).contains(TOKEN_STATIC_NONE) && StringUtils.deleteWhitespace(line).contains(TOKEN_PARSER_KEYS)) {
			if(line.indexOf(TOKEN_PARSER_KEYS) < line.indexOf(TOKEN_STATIC_NONE)) {
				return false;
			}else {
				return true;
			}
		}
		return true;
	}
}
