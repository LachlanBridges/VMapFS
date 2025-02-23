
class CommonUtils {
    static def norm(String str) {
        return str.replaceTrailingBrackets()
                  .replaceAll(/[`´‘’"“”]/, "'")
                  .replaceAll(/[:|]/, " - ")
                  .replaceAll(/[:]/, "\u2236") // Ratio symbol
                  .replaceAll(/[\/]/, "\u2215") // Division Slash
                  .replaceAll(/[?]/, "\uFF1F") // Fullwidth Question Mark
                  .replaceAll(/[\*]/, "\u204E") // Low asterisk
                  .replaceAll(/[*\s]+/, " ")
    }

    static def decade_range(Number year) {
        return year.toString().take(3) + "0-" + year.toString().take(3) + "9"
    }

    static Map<String, String> patterns = [
        'BluRay': 'Blu.?Ray|BDRip|BRip|BR.?Rip|BDMV|BD|BDR|BD25|BD50|BD5|BD9|3D.?BluRay|3DBD|REMUX',
        'CAM': '\\bCAM\\b|CAMRip|CAM.?Rip',
        'DVDRip': "DVD.?Rip|DVD.?Mux|\\bDVD\\b",
        'DVD-R': "DVD.?R|DVD.?Full|Full.?Rip|DVD.?[59]",
        'HDDVD': 'HDDVD',
        'HDTV': 'HDTV|DVB|DVBRip|DTVRip|HDTVRip',
        'LaserRip': 'LaserRip|Laserdisc',
        'MicroHD': 'MicroHD',
        'R5': 'R5|R5.?LINE',
        'SCREENER': 'SCREENER|\\bSCR\\b|DVDSCR|DVDSCREENER|BDSCR|BR.?Scr|BR.?Screener',
        'SDTV': 'SDTV|PDTV|DSR|DSRip|SATRip|DTHRip|TVRip',
        'TELECINE': 'TELECINE|\\bTC\\b|HDTC',
        'TELESYNC': 'TELESYNC|\\bTS\\b|HDTS|PDVD|PTVD|PreDVDRip',
        'UnknownRip': 'UnknownRip|URip',
        'VCD': '\\bVCD\\b',
        'VHS': '\\bVHS\\b|\\bVHSRip\\b',
        'WEB-DL': '(?:(?:ABC|ATVP|AMZN|BBC|CBS|CC|CR|CW|DCU|DSNP|DSNY|FBWatch|FREE|FOX|HMAX|HULU|iP|LIFE|MTV|NBC|NICK|NF|RED|TF1|STZ)[.-])?(?:WEB.?DL|WEB.?DLRip|WEB.?Cap|WEB.?Rip|HC|HD.?Rip|VODR|VODRip|PPV|PPVRip|www|iTunesHD|ithd|AmazonHD|NetflixHD|NetflixUHD|(?<=\\d{3,4}[p].)WEB|WEB(?=.[hx]\\d{3}))',
        'WorkPrint': 'WORKPRINT|WP'
    ]

    static String matchSource(String stringToMatch) {
        return patterns.findResults { key, pattern ->
            stringToMatch.find(pattern) ? key : null
        }.first()
    }

    static String getSourceString(String source, String vs, String fileName, String directoryName, String directoryParentName) {
        def remuxMatch = fileName.find(/(?i)REMUX/) ? ' REMUX' : ''
        def matchedFilenameSource = matchSource(fileName) ?: matchSource(directoryName) ?: matchSource(directoryParentName)
        def sourceString = source
        def vsString = vs
        if (vsString == 'WEB') {
            vsString = 'WEB-DL'
        }
        if (sourceString == 'WEB-DL') {
            sourceString = vsString
        }
        def finalSource = (sourceString ?: matchedFilenameSource) + remuxMatch
        return finalSource.replace('bluray', 'BluRay').replace('Bluray', 'BluRay').replace('Blu-ray', 'BluRay')
    }

    static String getVideoCodec(String videoCodec) {
        return videoCodec.replace('Microsoft', 'VC-1')
                         .replace('ATEME', 'x265')
                         .replace('AVC', 'x264')
                         .replace('H.264', 'x264')
                         .replace('HEVC.x265', 'x265')
                         .replace('HEVC', 'x265')
    }

    static String getVideoFormat(String videoFormat, Number bitDepth) {
        def vidFormat = videoFormat ?: ''
        if (bitDepth == 10) {
            vidFormat = vidFormat + ' 10bit'
        }
        return vidFormat
    }
}

def commonUtils = new CommonUtils()
def source_info = { commonUtils.getSourceString(source, vs, fn, f.dir.name, f.dir.dir.name) }
def video_codec = { commonUtils.getVideoCodec(vc) }
def video_format = { commonUtils.getVideoFormat(vf, bitdepth) }
