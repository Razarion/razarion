import {HttpMethodInterceptorArgs, InMemoryDbService, STATUS} from "angular-in-memory-web-api";
import {Response, ResponseOptions} from "@angular/http";
import {Observable} from "rxjs";

export class InMemoryDataService implements InMemoryDbService {
  sessions: any = [{
    "time": 1489795200000,
    "id": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
    "fbAdRazTrack": "2379NSCDB"
  }, {
    "time": 1489995200000,
    "id": "SGUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
    "fbAdRazTrack": "2379NSCDB"
  }];
  sessionDetail: any = {
    "time": 1489795200000,
    "id": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
    "fbAdRazTrack": "2379NSCDB",
    "gameSessionDetails": [
      {
        "time": 1489995200000,
        "id": "00000000000000000001",
        "sessionId": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/"
      },
      {
        "time": 1589995200000, "id": "00000000000000000002",
        "sessionId": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/"
      },
      {
        "time": 1689995200000, "id": "00000000000000000003",
        "sessionId": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/"
      },
      {
        "time": 1789995200000, "id": "00000000000000000004",
        "sessionId": "SZUGDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/"
      },
    ]
  };
  allbackupoverviews: any = [
    {
      "date": 1489795200000,
      "planetId": 2,
      "bases": 2,
      "items": 44,
    },
    {
      "date": 1482395200500,
      "planetId": 5,
      "bases": 1,
      "items": 2354,
    },
    {
      "date": 1482325200501,
      "planetId": 4,
      "bases": 4,
      "items": 2354,
    }
  ];
  dobackupanswer: any = [
    {
      "date": 1489795200000,
      "planetId": 2,
      "bases": 2,
      "items": 44,
    },
    {
      "date": 1482395200500,
      "planetId": 5,
      "bases": 1,
      "items": 2354,
    },
    {
      "date": 1482325200501,
      "planetId": 4,
      "bases": 4,
      "items": 2354,
    },
    {
      "date": 142325200501,
      "planetId": 4,
      "bases": 3,
      "items": 2354,
    }
  ];

  loadallonlines: any = [
    {
      "time": 1789995200000,
      "duration": 3601000,
      "humanPlayerId": {
        "playerId": 1111,
        "userId": 1234
      },
      "sessionId": "1++++GDHB3642307834GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
    },
    {
      "time": 142925200501,
      "duration": 61000,
      "humanPlayerId": {
        "playerId": 2222,
      },
      "sessionId": "2++++asddasaGDHB36423034GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
      "multiplayerPlanet": "Sirius (4)",
      "multiplayerDate": 1789995250000,
      "multiplayerDuration": 2300000,
    },
    {
      "time": 162925200501,
      "duration": 360000000,
      "humanPlayerId": {
        "playerId": 2222,
      },
      "sessionId": "3++++asddasaGDHB36423034GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
      "multiplayerPlanet": "Sirius (4)",
      "multiplayerDate": 1789995250000,
      "multiplayerDuration": 2300000,
    },
    {
      "time": 172925200501,
      "duration": 24000,
      "humanPlayerId": {
        "playerId": 2222,
      },
      "sessionId": "4++++asddasaGDHB36423034GZISBDGOIASWJDBGZHUBF)=(&*)=*RUOSBNDFOSW(DF/",
      "multiplayerPlanet": "Sirius (4)",
      "multiplayerDate": 1789995250000,
      "multiplayerDuration": 2300000,
    }
  ];

  createDb() {
    return this.sessions;
  }

  get(httpMethodInterceptorArgs: HttpMethodInterceptorArgs): Observable<Response> {
    return new Observable<Response>(observer => {
      let body;
      if (httpMethodInterceptorArgs.requestInfo.collectionName === "sessiondetail") {
        body = this.sessionDetail;
      } else if (httpMethodInterceptorArgs.requestInfo.collectionName === "allbackupoverviews") {
        body = this.allbackupoverviews;
      } else if (httpMethodInterceptorArgs.requestInfo.collectionName === "loadallonlines") {
        body = this.loadallonlines;
      } else {
        body = "unhandled get request in InMemoryDataService for: '" + httpMethodInterceptorArgs.requestInfo.collectionName + "'";
      }
      observer.next(new Response(new ResponseOptions({
        body: JSON.stringify(body),
        headers: httpMethodInterceptorArgs.requestInfo.headers,
        status: STATUS.OK
      })));
      observer.complete();
    });

  }

  post(httpMethodInterceptorArgs: HttpMethodInterceptorArgs): Observable<Response> {
    return new Observable<Response>(observer => {
      let body;
      if (httpMethodInterceptorArgs.requestInfo.collectionName === "sessions") {
        body = this.sessions;
      } else if (httpMethodInterceptorArgs.requestInfo.collectionName === "dobackup") {
        body = this.dobackupanswer;
      } else {
        body = "unhandled post request in InMemoryDataService for: '" + httpMethodInterceptorArgs.requestInfo.collectionName + "'";
      }
      observer.next(new Response(new ResponseOptions({
        body: JSON.stringify(body),
        headers: httpMethodInterceptorArgs.requestInfo.headers,
        status: STATUS.OK
      })));
      observer.complete();
    });

  }
}
