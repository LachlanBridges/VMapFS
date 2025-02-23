import CommonUtils

def commonUtils = new CommonUtils()

def norm = commonUtils.&norm

def series_name = {
    if (norm(n) != norm(primaryTitle)) {
        return norm(n.sortName().replaceTrailingBrackets()) + ' [' + norm(primaryTitle) + ']'
    }
    def localized_title = any{localize[any{info?.OriginalLanguage}{series.spokenLanguages}{languages[0]?.ISO2}{audio[0]?.language}]?.n}{n}
    if (norm(localized_title) != norm(n)) {
        return norm(n.sortName().replaceTrailingBrackets()) + ' [' + norm(localized_title) + ']'
    }
    return norm(n.sortName().replaceTrailingBrackets())
}

def series_year = {
    episodelist.findAll{it.season == 1}?.airdate?.year?.min() ?: y
}

def series_folder = {
    allOf{series_name}{'(' + series_year + ')'}.join(' ')
}

def season_folder = {
    if (episode.special) {
        'Specials'
    } else {
        allOf{'Season'}{s.pad(2)}{'(' + episodelist.findAll{it.season == s}?.airdate?.year?.min() + ')'}.join(' ')
    }
}

def category_folder = {
    def firstChar = n.sortName()[0].upper()
    firstChar =~ /[0-9]/ ? '0-9' : firstChar
}

def s00e00 = {
    episode.special ? 'S00E' + special.pad(2) : s00e00
}

def t = {
    if (type == 'MultiEpisode') {
        def titles = episodes.collect{it.title.replacePart(', Part $1')}
        def baseTitles = titles.collect{it.split(', Part ')[0]}
        def parts = titles.collect{it.split(', Part ')[1]}
        def uniqueTitles = baseTitles.unique()
        if (uniqueTitles.size() == 1) {
            return norm(uniqueTitles[0]) + ', Part ' + parts.join(' & ')
        }
    }
    return norm(t)
}

def main_title = {
    allOf{norm(n.replaceTrailingBrackets())}{s00e00}{t}.join(' - ')
}

def video_path = {
    allOf{'video/tv'}{category_folder}{series_folder}{season_folder}{main_title}.join('/')
}

def file_path = {
    allOf{video_path}{lang}.join('.').replaceAll(/null/,'')
}

def file_info = {
    "[" + allOf{source_info}{video_codec + ' ' + video_format}{audio_info}.join('] [') + "]"
}

def mylang = any{'.'+lang}{lang} + {fn =~ /(?i).+sdh.+/ ? '_SDH' : ''} + {fn =~ /(?i)\(foreignpartsonly\)/ ? '_ForeignPartsOnly' : ''}

/mnt/nas/' + file_path + ' ' + file_info + mylang