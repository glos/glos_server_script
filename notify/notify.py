import pika
import sys
import smtplib
import email.Message

_rabbitmq_host="localhost"
_subject="GLOS Data Harvesting Failure"
_from_mail="notification@glos.us"
_smtp="smtp.gmail.com:587"
_mail_user=("dataharv@glos.us","*8jdUX>U123")
_notify_error_exchange="glos_notify_data_status"
_notify_data_types=["glos_obs","glcfs","hecwfs","slrfvm"]
_notify_emails={
			"glos_obs":["gwang@glc.org","tslawecki@limno.com","kkoch@limno.com"],
			"glcfs":["gwang@glc.org"],
			"hecwfs":["gwang@glc.org"],
			"slrfvm":["gwang@glc.org"]
		}
connection = pika.BlockingConnection(pika.ConnectionParameters(host=_rabbitmq_host))
channel = connection.channel()
channel.exchange_declare(exchange=_notify_error_exchange,type='direct')
result = channel.queue_declare(exclusive=True)
queue_name = result.method.queue
for data_type in _notify_data_types:
    channel.queue_bind(exchange=_notify_error_exchange,queue=queue_name,routing_key=data_type)

def mailTo(mlist,body,logger):
    try:
	for mail in mlist:
	    smtp=smtplib.SMTP(_smtp)
	    if _mail_user[0]:
	        smtp.starttls()
	        smtp.login(_mail_user[0],_mail_user[1])
	    msg=email.Message.Message()
	    msg["To"]=mail
	    msg["From"]=_from_mail
	    msg["Subject"]=_subject
	    msg.set_payload(body)
	    smtp.sendmail(_from_mail,mail,msg.as_string())
	    #print("send mail to {0}".format(mail))
	    smtp.quit()
    except Exception as e:
	logger.write("Error during emailing:{0}".format(e))
    
def callback(ch, method, properties, body):
    #print " [x] %r:%r" % (method.routing_key, body,)
    with open('logs/notify-msg','a',0) as f:
        try:
            f.write(" [x] %r:%r\n" % (method.routing_key, body,))
            if _notify_emails.get(method.routing_key) is not None:
	        mailTo(_notify_emails[method.routing_key],body,f)
        except Exception as e:
	    pass
	
channel.basic_consume(callback,queue=queue_name,no_ack=True)
channel.start_consuming()

