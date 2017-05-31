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

  createDb() {
    return this.sessions;
  }

  get(httpMethodInterceptorArgs: HttpMethodInterceptorArgs): Observable<Response> {
    return new Observable<Response>(observer => {
      let body;
      if (httpMethodInterceptorArgs.requestInfo.collectionName === "sessions") {
        body = this.sessions;
      } else if (httpMethodInterceptorArgs.requestInfo.collectionName === "sessiondetail") {
        body = this.sessionDetail;
      } else {
        body = "unhandled request in InMemoryDataService";
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
