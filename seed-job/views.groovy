// ###################### VIEWS

listView('DEVELOP') {
    jobs {
        regex(/.*-DEVELOP/)
    }
    columns {
        buildButton()
        status()
        progressBar()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
    }
}

listView('ADMIN') {
    jobs {
        regex(/ADMIN-.*/)
    }
    columns {
        buildButton()
        status()
        progressBar()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
    }
}
