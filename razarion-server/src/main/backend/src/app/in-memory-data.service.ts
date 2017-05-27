import {InMemoryDbService} from "angular-in-memory-web-api";

export class InMemoryDataService implements InMemoryDbService {

  createDb() {
    let sessions = [{
      "time": 1489795200000,
      "id": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
      "fbAdRazTrack": "2379NSCDB"
    },{
      "time": 1489995200000,
      "id": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
      "fbAdRazTrack": "2379NSCDB"
    }];

    return {sessions};
  }

}
