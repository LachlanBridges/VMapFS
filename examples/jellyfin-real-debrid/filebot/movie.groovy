import CommonUtils

def commonUtils = new CommonUtils()

def movie_name = {
    if (commonUtils.norm(n) != commonUtils.norm(primaryTitle)) {
        return commonUtils.norm(n.replaceTrailingBrackets()) + ' [' + commonUtils.norm(primaryTitle) + ']'
    }
    return commonUtils.norm(n.replaceTrailingBrackets())
}

def movie_sortname = {
    if (commonUtils.norm(n) != commonUtils.norm(primaryTitle)) {
        return commonUtils.norm(n.sortName().replaceTrailingBrackets()) + ' [' + commonUtils.norm(primaryTitle) + ']'
    }
    return commonUtils.norm(n.sortName().replaceTrailingBrackets())
}

def category_folder = { commonUtils.decade_range(y) }

def movie_folder = {
    allOf{movie_sortname}{any{'('+y+')'}{'(UNKNOWN YEAR)'}}{'['+tags.join(', ') + ']'}.join(' ')
}

def full_movie_title = {
    allOf{movie_name}{any{'('+y+')'}{'(UNKNOWN YEAR)'}}{'['+tags.join(', ') + ']'}{'- Part ' + pi}.join(' ')
}

def movie_video_path = {
    allOf{'video/movies'}{category_folder}{movie_folder}{full_movie_title}.join('/')
}

def video_path = movie_video_path

def file_info = {
    "[" + allOf{source_info}{video_codec + ' ' + video_format}{audio_info}.join('] [') + "]"
}

def mylang = any{'.'+lang}{lang} + {fn =~ /(?i).+sdh.+/ ? '_SDH' : ''} + {fn =~ /(?i)\(foreignpartsonly\)/ ? '_ForeignPartsOnly' : ''}

'/mnt/nas/' + video_path + ' ' + file_info + mylang
