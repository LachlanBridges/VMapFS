
import CommonUtils

def commonUtils = new CommonUtils()

def norm = commonUtils.&norm

def series_name = {
    norm(n.replaceTrailingBrackets())
}

def series_folder = {
    allOf{series_name}{'(' + y + ')'}.join(' ')
}

def episode_title = {
    if (episode.special) {
        'S00E' + special.pad(2) + ' - ' + norm(t)
    } else {
        'S' + s.pad(2) + 'E' + e.pad(2) + ' - ' + norm(t)
    }
}

def video_path = {
    allOf{'video/anime'}{series_folder}{episode_title}.join('/')
}

def file_info = {
    "[" + allOf{source_info}{video_codec + ' ' + video_format}{audio_info}.join('] [') + "]"
}

def mylang = any{'.'+lang}{lang} + {fn =~ /(?i).+sdh.+/ ? '_SDH' : ''}

'/mnt/nas/' + video_path + ' ' + file_info + mylang
